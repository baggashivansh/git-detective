package com.gitdetective.evidence.builder;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.evidence.collector.EvidenceCollector;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceRecord;
import com.gitdetective.evidence.model.EvidenceSections;
import com.gitdetective.evidence.model.EvidenceVerificationStatus;
import com.gitdetective.evidence.validator.EvidenceValidator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Assembles an {@link EvidenceBundle} by orchestrating collectors. Contains no investigation
 * business logic.
 */
@Component
public class EvidenceBundleBuilder {

    private final List<EvidenceCollector> collectors;
    private final EvidenceValidator validator;
    private final InvestigationEvidenceMapper mapper;
    private final String engineVersion;

    public EvidenceBundleBuilder(
            List<EvidenceCollector> collectors,
            EvidenceValidator validator,
            InvestigationEvidenceMapper mapper,
            @Value("${gitdetective.application.version:1.0.0}") String engineVersion) {
        this.collectors = List.copyOf(collectors);
        this.validator = validator;
        this.mapper = mapper;
        this.engineVersion = engineVersion;
    }

    public EvidenceBundle build(InvestigationDetailResponse detail, CodeRepository repository) {
        return build(detail, repository, false);
    }

    public EvidenceBundle build(
            InvestigationDetailResponse detail, CodeRepository repository, boolean fromCache) {
        mapper.requireDetail(detail);
        Objects.requireNonNull(repository, "repository");

        InvestigationSummaryResponse summary = detail.summary();
        UUID investigationId = summary.id();
        UUID repositoryId = summary.repositoryId();

        if (!repository.getId().equals(repositoryId)) {
            throw new IllegalArgumentException(
                    "Repository mismatch: detail.repositoryId="
                            + repositoryId
                            + " repository.id="
                            + repository.getId());
        }

        List<EvidenceRecord> collected = new ArrayList<>();
        for (EvidenceCollector collector : collectors) {
            collected.addAll(collector.collect(detail, repository));
        }

        List<EvidenceRecord> verified =
                validator.validateAndMark(collected, repositoryId, investigationId);

        EvidenceSections.RepositoryInformation repositoryInformation =
                new EvidenceSections.RepositoryInformation(
                        repository.getId(),
                        repository.getName(),
                        repository.getSourceType().name(),
                        repository.getSourceUri(),
                        repository.getDefaultBranch(),
                        repository.getPrimaryLanguage(),
                        repository.getTotalCommits(),
                        repository.getStatus().name(),
                        repository.getAnalyzedAt());

        EvidenceSections.InvestigationTargetSection targetSection =
                new EvidenceSections.InvestigationTargetSection(
                        investigationId,
                        summary.targetType().name(),
                        summary.targetRef(),
                        summary.targetLabel(),
                        summary.status().name(),
                        summary.summary(),
                        summary.createdAt(),
                        summary.completedAt());

        EvidenceSections.TimelineSection timeline =
                new EvidenceSections.TimelineSection(filter(verified, EvidenceCategory.TIMELINE));
        EvidenceSections.OwnershipSection ownership =
                new EvidenceSections.OwnershipSection(
                        filter(verified, EvidenceCategory.OWNERSHIP),
                        summary.busFactorScore(),
                        summary.busFactorLevel() == null ? null : summary.busFactorLevel().name(),
                        "Bus factor = fewest top contributors covering >=50% ownership mass");
        EvidenceSections.ImpactSection impact =
                new EvidenceSections.ImpactSection(
                        filter(verified, EvidenceCategory.IMPACT),
                        summary.blastRadiusScore(),
                        "Blast radius from indexed dependency edges and imports");
        EvidenceSections.RelationshipSection relationships =
                new EvidenceSections.RelationshipSection(
                        filter(verified, EvidenceCategory.RELATIONSHIP));
        EvidenceSections.DependencySection dependencies =
                new EvidenceSections.DependencySection(
                        filterAny(verified, EvidenceCategory.DEPENDENCY, EvidenceCategory.IMPORT));
        EvidenceSections.PackageHealthSection packageHealth =
                new EvidenceSections.PackageHealthSection(
                        filter(verified, EvidenceCategory.PACKAGE_HEALTH));
        EvidenceSections.HotspotSection hotspots =
                new EvidenceSections.HotspotSection(filter(verified, EvidenceCategory.HOTSPOT));

        List<EvidenceRecord> statisticRecords = filter(verified, EvidenceCategory.STATISTIC);
        EvidenceSections.StatisticsSection statistics =
                new EvidenceSections.StatisticsSection(
                        detail.evidence().size(),
                        detail.timeline().size(),
                        detail.ownership().size(),
                        detail.impact().size(),
                        detail.relationships().size(),
                        detail.hotspots().size(),
                        detail.packageHealth().size(),
                        detail.commitClusters().size(),
                        detail.traces().size(),
                        statisticRecords);

        EvidenceSections.SupportingRefs supportingRefs = extractSupportingRefs(verified);

        int averageConfidence =
                verified.isEmpty()
                        ? 0
                        : (int)
                                Math.round(
                                        verified.stream()
                                                .mapToInt(EvidenceRecord::confidence)
                                                .average()
                                                .orElse(0));

        EvidenceSections.BundleMetadata metadata =
                new EvidenceSections.BundleMetadata(
                        UUID.randomUUID(),
                        Instant.now(),
                        engineVersion,
                        verified.size(),
                        averageConfidence,
                        fromCache);

        Set<String> provenances =
                verified.stream()
                        .map(r -> r.source().name())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        long verifiedCount =
                verified.stream()
                        .filter(r -> r.verificationStatus() == EvidenceVerificationStatus.VERIFIED)
                        .count();

        String factualOverview =
                "Evidence bundle for investigation "
                        + investigationId
                        + " on repository "
                        + repository.getName()
                        + " target="
                        + summary.targetType()
                        + ":"
                        + summary.targetRef()
                        + " items="
                        + verified.size()
                        + " avgConfidence="
                        + averageConfidence;

        EvidenceSections.EvidenceSummary evidenceSummary =
                new EvidenceSections.EvidenceSummary(
                        factualOverview, (int) verifiedCount, 0, List.copyOf(provenances));

        EvidenceBundle bundle =
                new EvidenceBundle(
                        investigationId,
                        repositoryId,
                        repositoryInformation,
                        targetSection,
                        timeline,
                        ownership,
                        impact,
                        relationships,
                        dependencies,
                        packageHealth,
                        hotspots,
                        statistics,
                        supportingRefs,
                        metadata,
                        evidenceSummary,
                        verified);

        validator.validateBundle(bundle);
        return bundle;
    }

    private static List<EvidenceRecord> filter(
            List<EvidenceRecord> records, EvidenceCategory category) {
        return records.stream().filter(r -> r.evidenceType() == category).toList();
    }

    private static List<EvidenceRecord> filterAny(
            List<EvidenceRecord> records, EvidenceCategory... categories) {
        Set<EvidenceCategory> set = Set.of(categories);
        return records.stream().filter(r -> set.contains(r.evidenceType())).toList();
    }

    private EvidenceSections.SupportingRefs extractSupportingRefs(List<EvidenceRecord> records) {
        LinkedHashSet<String> commits = new LinkedHashSet<>();
        LinkedHashSet<String> files = new LinkedHashSet<>();
        LinkedHashSet<String> packages = new LinkedHashSet<>();
        LinkedHashSet<String> classes = new LinkedHashSet<>();
        LinkedHashSet<String> methods = new LinkedHashSet<>();
        LinkedHashSet<String> contributors = new LinkedHashSet<>();

        for (EvidenceRecord record : records) {
            String ref = record.sourceIdentifier();
            switch (record.evidenceType()) {
                case COMMIT, TIMELINE, CLUSTER -> commits.add(ref);
                case FILE -> files.add(ref);
                case PACKAGE, PACKAGE_HEALTH -> packages.add(ref);
                case CLASS -> classes.add(ref);
                case METHOD -> methods.add(ref);
                case CONTRIBUTOR, OWNERSHIP -> contributors.add(ref);
                default -> {
                    String kind =
                            record.supportingMetadata()
                                    .getOrDefault("sourceKind", "")
                                    .toUpperCase(Locale.ROOT);
                    if (kind.contains("FILE")) {
                        files.add(ref);
                    }
                }
            }
            String sha = record.supportingMetadata().get("commitSha");
            if (sha != null && !sha.isBlank()) {
                commits.add(sha);
            }
            String email = record.supportingMetadata().get("actorEmail");
            if (email != null && !email.isBlank()) {
                contributors.add(email);
            }
        }

        return new EvidenceSections.SupportingRefs(
                List.copyOf(commits),
                List.copyOf(files),
                List.copyOf(packages),
                List.copyOf(classes),
                List.copyOf(methods),
                List.copyOf(contributors));
    }
}
