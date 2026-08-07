package com.gitdetective.assistant.context;

import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Builds compact AI context exclusively from an {@link EvidenceBundle}. Never queries other
 * systems.
 */
@Component
public class EvidenceContextBuilder {

    private static final int MAX_ITEMS_PER_SECTION = 25;

    public EvidenceContext build(EvidenceBundle bundle, AssistantIntent intent) {
        Set<EvidenceCategory> priorities = prioritiesFor(intent);
        List<EvidenceRecord> selected = new ArrayList<>();

        for (EvidenceRecord record : bundle.allEvidence()) {
            if (priorities.contains(record.evidenceType())) {
                selected.add(record);
            }
        }
        if (selected.isEmpty()) {
            selected.addAll(bundle.allEvidence());
        }
        if (selected.size() > MAX_ITEMS_PER_SECTION * 3) {
            selected = selected.subList(0, MAX_ITEMS_PER_SECTION * 3);
        }

        String compact =
                selected.stream().map(this::formatRecord).collect(Collectors.joining("\n"));

        return new EvidenceContext(
                bundle.investigationId().toString(),
                bundle.repositoryId().toString(),
                bundle.investigationTarget().targetType(),
                bundle.investigationTarget().targetRef(),
                bundle.evidenceSummary().factualOverview(),
                compact,
                selected,
                List.copyOf(bundle.supportingCommits()),
                List.copyOf(bundle.supportingFiles()),
                List.copyOf(bundle.supportingPackages()),
                List.copyOf(bundle.supportingContributors()),
                averageConfidence(selected));
    }

    private Set<EvidenceCategory> prioritiesFor(AssistantIntent intent) {
        return switch (intent) {
            case OWNERSHIP ->
                    EnumSet.of(
                            EvidenceCategory.OWNERSHIP,
                            EvidenceCategory.CONTRIBUTOR,
                            EvidenceCategory.STATISTIC);
            case TIMELINE ->
                    EnumSet.of(
                            EvidenceCategory.TIMELINE,
                            EvidenceCategory.COMMIT,
                            EvidenceCategory.CLUSTER);
            case IMPACT ->
                    EnumSet.of(
                            EvidenceCategory.IMPACT,
                            EvidenceCategory.DEPENDENCY,
                            EvidenceCategory.RELATIONSHIP);
            case RELATIONSHIP, ARCHITECTURE ->
                    EnumSet.of(
                            EvidenceCategory.RELATIONSHIP,
                            EvidenceCategory.DEPENDENCY,
                            EvidenceCategory.IMPORT,
                            EvidenceCategory.CLASS,
                            EvidenceCategory.PACKAGE);
            case AUTHENTICATION, REQUEST_FLOW ->
                    EnumSet.of(
                            EvidenceCategory.TRACE,
                            EvidenceCategory.CONFIG,
                            EvidenceCategory.CLASS);
            case PACKAGE_HEALTH ->
                    EnumSet.of(
                            EvidenceCategory.PACKAGE_HEALTH,
                            EvidenceCategory.PACKAGE,
                            EvidenceCategory.STATISTIC);
            case HOTSPOT ->
                    EnumSet.of(
                            EvidenceCategory.HOTSPOT,
                            EvidenceCategory.FILE,
                            EvidenceCategory.CLASS);
            case STATISTICS -> EnumSet.of(EvidenceCategory.STATISTIC, EvidenceCategory.REPOSITORY);
            case SUMMARY, GENERAL_INVESTIGATION, UNKNOWN -> EnumSet.allOf(EvidenceCategory.class);
        };
    }

    private String formatRecord(EvidenceRecord record) {
        return "[id="
                + record.evidenceId()
                + " type="
                + record.evidenceType()
                + " confidence="
                + record.confidence()
                + " provenance="
                + record.source()
                + "] "
                + record.description()
                + " ref="
                + record.sourceIdentifier();
    }

    private static int averageConfidence(List<EvidenceRecord> records) {
        if (records.isEmpty()) {
            return 0;
        }
        return (int)
                Math.round(
                        records.stream().mapToInt(EvidenceRecord::confidence).average().orElse(0));
    }

    public record EvidenceContext(
            String investigationId,
            String repositoryId,
            String targetType,
            String targetRef,
            String factualOverview,
            String compactEvidence,
            List<EvidenceRecord> selectedEvidence,
            List<String> supportingCommits,
            List<String> supportingFiles,
            List<String> supportingPackages,
            List<String> supportingContributors,
            int averageConfidence) {}
}
