package com.gitdetective.incident;

import com.gitdetective.assistant.validator.AssistantEvidenceValidator;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator.ValidatedAiResponse;
import com.gitdetective.dto.response.IncidentReportResponse;
import com.gitdetective.dto.response.IncidentReportResponse.BlastRadius;
import com.gitdetective.dto.response.IncidentReportResponse.Claim;
import com.gitdetective.dto.response.IncidentReportResponse.EvidenceEntry;
import com.gitdetective.dto.response.IncidentReportResponse.OwnershipEntry;
import com.gitdetective.dto.response.IncidentReportResponse.TimelineEntry;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationDetailResponse.ImpactItem;
import com.gitdetective.dto.response.InvestigationDetailResponse.OwnershipItem;
import com.gitdetective.dto.response.InvestigationDetailResponse.PackageHealthItem;
import com.gitdetective.dto.response.InvestigationDetailResponse.TimelineItem;
import com.gitdetective.entity.RiskLevel;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Builds a structured incident report from investigation slices and an Evidence Bundle. AI text is
 * optional and never used as a source of new repository facts.
 */
@Component
public class IncidentReportCompiler {

    static final String INSUFFICIENT = AssistantEvidenceValidator.INSUFFICIENT;
    static final String TIMELINE_NOTE =
            "Timestamps below are repository history (commit/index time). They are not"
                    + " deployment or incident clocks unless the user separately provided those.";

    public IncidentReportResponse compile(
            String question,
            InvestigationDetailResponse detail,
            EvidenceBundle bundle,
            ValidatedAiResponse ai) {
        List<EvidenceRecord> evidence = bundle == null ? List.of() : bundle.allEvidence();
        boolean insufficient = evidence.isEmpty() && detail.evidence().isEmpty();
        ValidatedAiResponse usableAi = ai != null && !ai.insufficientEvidence() ? ai : null;

        String finding = finding(question, detail, usableAi, insufficient);
        int confidence = confidence(detail, bundle, usableAi, insufficient);
        String why = why(question, detail, insufficient);
        List<TimelineEntry> timeline = timeline(detail);
        List<EvidenceEntry> keyEvidence = keyEvidence(detail, evidence);
        List<String> affectedFiles = affectedFiles(detail, bundle);
        List<String> affectedComponents = affectedComponents(detail, bundle);
        List<OwnershipEntry> ownership = ownership(detail);
        BlastRadius blastRadius = blastRadius(detail);
        List<String> riskFactors = riskFactors(question, detail);
        List<String> actions = nextActions(question, detail, insufficient);
        List<String> limitations = limitations(detail, bundle, insufficient);
        List<Claim> claims = claims(detail, evidence, finding, insufficient);

        return new IncidentReportResponse(
                finding,
                confidence,
                why,
                TIMELINE_NOTE,
                timeline,
                keyEvidence,
                affectedFiles,
                affectedComponents,
                ownership,
                blastRadius,
                riskFactors,
                actions,
                limitations,
                claims,
                insufficient);
    }

    private static String finding(
            String question,
            InvestigationDetailResponse detail,
            ValidatedAiResponse ai,
            boolean insufficient) {
        if (insufficient) {
            return INSUFFICIENT;
        }
        if (ai != null && ai.answer() != null && !ai.answer().isBlank()) {
            return ai.answer().strip();
        }
        String target = detail.summary().targetLabel();
        String type = detail.summary().targetType().name();
        return "Indexed repository evidence for \""
                + question
                + "\" was collected against "
                + type
                + " '"
                + target
                + "'. "
                + (detail.summary().summary() == null ? "" : detail.summary().summary());
    }

    private static int confidence(
            InvestigationDetailResponse detail,
            EvidenceBundle bundle,
            ValidatedAiResponse ai,
            boolean insufficient) {
        if (insufficient) {
            return 0;
        }
        int base = 40;
        if (bundle != null && bundle.metadata() != null) {
            base = Math.max(base, bundle.metadata().averageConfidence());
        }
        if (!detail.timeline().isEmpty()) {
            base = Math.min(100, base + 10);
        }
        if (!detail.ownership().isEmpty()) {
            base = Math.min(100, base + 5);
        }
        if (ai != null) {
            return Math.min(base, ai.confidence());
        }
        return base;
    }

    private static String why(
            String question, InvestigationDetailResponse detail, boolean insufficient) {
        if (insufficient) {
            return "The investigation did not produce enough indexed evidence to support a"
                    + " confident causal answer.";
        }
        StringBuilder why = new StringBuilder();
        why.append("FACT: investigation target is ")
                .append(detail.summary().targetType())
                .append(" '")
                .append(detail.summary().targetLabel())
                .append("'. ");
        why.append("FACT: ")
                .append(detail.timeline().size())
                .append(" repository timeline event(s) and ")
                .append(detail.evidence().size())
                .append(" evidence item(s) were persisted. ");
        if (detail.summary().busFactorScore() != null) {
            why.append("FACT: calculated bus factor is ")
                    .append(detail.summary().busFactorScore())
                    .append(" (")
                    .append(detail.summary().busFactorLevel())
                    .append("). ");
        }
        if (detail.summary().blastRadiusScore() != null) {
            why.append("FACT: calculated blast-radius score is ")
                    .append(detail.summary().blastRadiusScore())
                    .append(". ");
        }
        if (asksAboutAuthentication(question)) {
            boolean authTrace =
                    detail.traces().stream()
                            .anyMatch(trace -> "AUTH_FLOW".equals(trace.traceKind()));
            if (!authTrace) {
                why.append("FACT: no authentication-flow trace was detected for this target. ");
                why.append(
                        "HYPOTHESIS: risk language in the question is not independently proven by");
                why.append(" repository evidence.");
            } else {
                why.append(
                        "FACT: an authentication-flow trace was detected from indexed annotations");
                why.append(" or types.");
            }
        }
        return why.toString();
    }

    static boolean asksAboutAuthentication(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }
        String q = question.toLowerCase(Locale.ROOT);
        return q.contains("auth")
                || q.contains("login")
                || q.contains("jwt")
                || q.contains("security");
    }

    private static List<TimelineEntry> timeline(InvestigationDetailResponse detail) {
        List<TimelineEntry> entries = new ArrayList<>();
        for (TimelineItem item : detail.timeline()) {
            entries.add(
                    new TimelineEntry(
                            item.occurredAt(),
                            item.title(),
                            item.detail(),
                            item.commitSha(),
                            ClaimStrength.FACT));
            if (entries.size() >= 20) {
                break;
            }
        }
        return List.copyOf(entries);
    }

    private static List<EvidenceEntry> keyEvidence(
            InvestigationDetailResponse detail, List<EvidenceRecord> bundleEvidence) {
        List<EvidenceEntry> entries = new ArrayList<>();
        for (EvidenceRecord record : bundleEvidence) {
            entries.add(
                    new EvidenceEntry(
                            record.evidenceId(),
                            record.evidenceType().name(),
                            record.description(),
                            ClaimStrength.FACT));
            if (entries.size() >= 12) {
                return List.copyOf(entries);
            }
        }
        for (var item : detail.evidence()) {
            entries.add(
                    new EvidenceEntry(item.id(), item.label(), item.detail(), ClaimStrength.FACT));
            if (entries.size() >= 12) {
                break;
            }
        }
        return List.copyOf(entries);
    }

    private static List<String> affectedFiles(
            InvestigationDetailResponse detail, EvidenceBundle bundle) {
        LinkedHashSet<String> files = new LinkedHashSet<>();
        if (detail.summary().targetType().name().equals("FILE")) {
            files.add(detail.summary().targetLabel());
        }
        for (ImpactItem item : detail.impact()) {
            if ("FILE".equalsIgnoreCase(item.itemKind())
                    || (item.itemLabel() != null && item.itemLabel().contains("/"))) {
                files.add(item.itemLabel());
            }
        }
        if (bundle != null) {
            files.addAll(bundle.supportingFiles());
        }
        return List.copyOf(files).stream().limit(20).toList();
    }

    private static List<String> affectedComponents(
            InvestigationDetailResponse detail, EvidenceBundle bundle) {
        LinkedHashSet<String> components = new LinkedHashSet<>();
        components.add(detail.summary().targetType() + ":" + detail.summary().targetLabel());
        for (ImpactItem item : detail.impact()) {
            components.add(item.itemKind() + ":" + item.itemLabel());
        }
        for (var health : detail.packageHealth()) {
            if (health.riskLevel() == RiskLevel.HIGH || health.riskLevel() == RiskLevel.MEDIUM) {
                components.add("PACKAGE:" + health.packageName());
            }
        }
        if (bundle != null) {
            bundle.supportingPackages().forEach(name -> components.add("PACKAGE:" + name));
            bundle.supportingClasses().forEach(name -> components.add("CLASS:" + name));
        }
        return List.copyOf(components).stream().limit(20).toList();
    }

    private static List<OwnershipEntry> ownership(InvestigationDetailResponse detail) {
        List<OwnershipEntry> owners = new ArrayList<>();
        for (OwnershipItem item : detail.ownership()) {
            owners.add(
                    new OwnershipEntry(
                            item.contributorName(),
                            item.contributorEmail(),
                            item.ownershipPercentage(),
                            item.ownershipKind() == null ? null : item.ownershipKind().name()));
        }
        return List.copyOf(owners);
    }

    private static BlastRadius blastRadius(InvestigationDetailResponse detail) {
        List<String> items =
                detail.impact().stream()
                        .map(
                                item ->
                                        item.itemKind()
                                                + " "
                                                + item.itemLabel()
                                                + " — "
                                                + item.reason())
                        .limit(15)
                        .toList();
        return new BlastRadius(
                detail.summary().blastRadiusScore(),
                items,
                "Blast radius is computed from the indexed dependency graph, not from runtime"
                        + " traffic or deployments.");
    }

    private static List<String> riskFactors(String question, InvestigationDetailResponse detail) {
        List<String> factors = new ArrayList<>();
        if (detail.summary().busFactorLevel() != null) {
            factors.add(
                    "FACT: bus-factor level is "
                            + detail.summary().busFactorLevel()
                            + " (score "
                            + detail.summary().busFactorScore()
                            + "). This is code-ownership concentration, not personal blame.");
        }
        for (var hotspot : detail.hotspots()) {
            if (hotspot.rankPosition() <= 3) {
                factors.add(
                        "FACT: hotspot "
                                + hotspot.itemLabel()
                                + " rank="
                                + hotspot.rankPosition()
                                + " score="
                                + hotspot.score());
            }
        }
        for (PackageHealthItem health : detail.packageHealth()) {
            if (health.riskLevel() == RiskLevel.HIGH) {
                factors.add("FACT: package " + health.packageName() + " has HIGH calculated risk.");
            }
        }
        if (asksAboutAuthentication(question)) {
            boolean authTrace =
                    detail.traces().stream()
                            .anyMatch(trace -> "AUTH_FLOW".equals(trace.traceKind()));
            if (!authTrace) {
                factors.add(
                        "FACT: authentication flow was not detected in indexed traces for this"
                                + " target.");
            }
        }
        return List.copyOf(factors);
    }

    private static List<String> nextActions(
            String question, InvestigationDetailResponse detail, boolean insufficient) {
        List<String> actions = new ArrayList<>();
        if (insufficient) {
            actions.add("Analyze a repository that contains the relevant source and history.");
            actions.add("Ask a more specific question naming a file, class, or package.");
            return List.copyOf(actions);
        }
        if (asksAboutAuthentication(question)) {
            boolean authTrace =
                    detail.traces().stream()
                            .anyMatch(trace -> "AUTH_FLOW".equals(trace.traceKind()));
            if (!authTrace) {
                actions.add(
                        "If authentication is in scope, investigate a class or file whose path or"
                                + " name contains auth, security, or login.");
            }
        }
        if (!detail.hotspots().isEmpty()) {
            actions.add(
                    "Inspect the top hotspot "
                            + detail.hotspots().get(0).itemLabel()
                            + " as a follow-up target.");
        }
        if (detail.summary().busFactorLevel() != null
                && "HIGH".equals(detail.summary().busFactorLevel().name())) {
            actions.add(
                    "Review code-ownership concentration on this target before further changes.");
        }
        actions.add(
                "Do not treat this report as a deploy postmortem without operational timestamps.");
        return List.copyOf(actions);
    }

    private static List<String> limitations(
            InvestigationDetailResponse detail, EvidenceBundle bundle, boolean insufficient) {
        List<String> limits = new ArrayList<>();
        limits.add("Only public GitHub or server-local git sources are analyzed.");
        limits.add("Structural parsing is Java-oriented; other languages have metadata only.");
        limits.add("Repository timestamps are distinct from any user-provided incident time.");
        limits.add("No deployment, production, or runtime telemetry is available.");
        if (insufficient) {
            limits.add("Evidence was insufficient for a confident answer.");
        }
        if (detail.traces().isEmpty()) {
            limits.add("No request or authentication flow was reconstructed.");
        }
        if (bundle != null && bundle.allEvidence().isEmpty()) {
            limits.add("The Evidence Bundle contained no records.");
        }
        return List.copyOf(limits);
    }

    private static List<Claim> claims(
            InvestigationDetailResponse detail,
            List<EvidenceRecord> evidence,
            String finding,
            boolean insufficient) {
        List<Claim> claims = new ArrayList<>();
        List<UUID> ids = evidence.stream().map(EvidenceRecord::evidenceId).limit(8).toList();
        claims.add(
                new Claim(
                        "Investigation target is "
                                + detail.summary().targetType()
                                + " '"
                                + detail.summary().targetLabel()
                                + "'.",
                        ClaimStrength.FACT,
                        ids));
        if (insufficient) {
            claims.add(new Claim(INSUFFICIENT, ClaimStrength.FACT, List.of()));
            return List.copyOf(claims);
        }
        if (finding != null && finding.toLowerCase(Locale.ROOT).contains("hypothesis")) {
            claims.add(new Claim(finding, ClaimStrength.HYPOTHESIS, ids));
        } else if (finding != null) {
            claims.add(new Claim(finding, ClaimStrength.STRONG_INFERENCE, ids));
        }
        return List.copyOf(claims);
    }
}
