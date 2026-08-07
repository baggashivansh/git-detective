package com.gitdetective.evidence.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Immutable complete investigation evidence pack for future AI consumption.
 *
 * <p>Construct exclusively via {@link com.gitdetective.evidence.builder.EvidenceBundleBuilder}.
 */
public final class EvidenceBundle {

    private final UUID investigationId;
    private final UUID repositoryId;
    private final EvidenceSections.RepositoryInformation repositoryInformation;
    private final EvidenceSections.InvestigationTargetSection investigationTarget;
    private final EvidenceSections.TimelineSection timeline;
    private final EvidenceSections.OwnershipSection ownership;
    private final EvidenceSections.ImpactSection impact;
    private final EvidenceSections.RelationshipSection relationships;
    private final EvidenceSections.DependencySection dependencies;
    private final EvidenceSections.PackageHealthSection packageHealth;
    private final EvidenceSections.HotspotSection hotspots;
    private final EvidenceSections.StatisticsSection statistics;
    private final EvidenceSections.SupportingRefs supportingRefs;
    private final EvidenceSections.BundleMetadata metadata;
    private final EvidenceSections.EvidenceSummary evidenceSummary;
    private final List<EvidenceRecord> allEvidence;

    public EvidenceBundle(
            UUID investigationId,
            UUID repositoryId,
            EvidenceSections.RepositoryInformation repositoryInformation,
            EvidenceSections.InvestigationTargetSection investigationTarget,
            EvidenceSections.TimelineSection timeline,
            EvidenceSections.OwnershipSection ownership,
            EvidenceSections.ImpactSection impact,
            EvidenceSections.RelationshipSection relationships,
            EvidenceSections.DependencySection dependencies,
            EvidenceSections.PackageHealthSection packageHealth,
            EvidenceSections.HotspotSection hotspots,
            EvidenceSections.StatisticsSection statistics,
            EvidenceSections.SupportingRefs supportingRefs,
            EvidenceSections.BundleMetadata metadata,
            EvidenceSections.EvidenceSummary evidenceSummary,
            List<EvidenceRecord> allEvidence) {
        this.investigationId = Objects.requireNonNull(investigationId);
        this.repositoryId = Objects.requireNonNull(repositoryId);
        this.repositoryInformation = Objects.requireNonNull(repositoryInformation);
        this.investigationTarget = Objects.requireNonNull(investigationTarget);
        this.timeline = Objects.requireNonNull(timeline);
        this.ownership = Objects.requireNonNull(ownership);
        this.impact = Objects.requireNonNull(impact);
        this.relationships = Objects.requireNonNull(relationships);
        this.dependencies = Objects.requireNonNull(dependencies);
        this.packageHealth = Objects.requireNonNull(packageHealth);
        this.hotspots = Objects.requireNonNull(hotspots);
        this.statistics = Objects.requireNonNull(statistics);
        this.supportingRefs =
                Objects.requireNonNullElse(supportingRefs, EvidenceSections.SupportingRefs.empty());
        this.metadata = Objects.requireNonNull(metadata);
        this.evidenceSummary = Objects.requireNonNull(evidenceSummary);
        this.allEvidence = List.copyOf(Objects.requireNonNull(allEvidence));
    }

    public UUID investigationId() {
        return investigationId;
    }

    public UUID repositoryId() {
        return repositoryId;
    }

    public EvidenceSections.RepositoryInformation repositoryInformation() {
        return repositoryInformation;
    }

    public EvidenceSections.InvestigationTargetSection investigationTarget() {
        return investigationTarget;
    }

    public EvidenceSections.TimelineSection timeline() {
        return timeline;
    }

    public EvidenceSections.OwnershipSection ownership() {
        return ownership;
    }

    public EvidenceSections.ImpactSection impact() {
        return impact;
    }

    public EvidenceSections.RelationshipSection relationships() {
        return relationships;
    }

    public EvidenceSections.DependencySection dependencies() {
        return dependencies;
    }

    public EvidenceSections.PackageHealthSection packageHealth() {
        return packageHealth;
    }

    public EvidenceSections.HotspotSection hotspots() {
        return hotspots;
    }

    public EvidenceSections.StatisticsSection statistics() {
        return statistics;
    }

    public EvidenceSections.SupportingRefs supportingRefs() {
        return supportingRefs;
    }

    public List<String> supportingCommits() {
        return supportingRefs.commits();
    }

    public List<String> supportingFiles() {
        return supportingRefs.files();
    }

    public List<String> supportingPackages() {
        return supportingRefs.packages();
    }

    public List<String> supportingClasses() {
        return supportingRefs.classes();
    }

    public List<String> supportingMethods() {
        return supportingRefs.methods();
    }

    public List<String> supportingContributors() {
        return supportingRefs.contributors();
    }

    public EvidenceSections.BundleMetadata metadata() {
        return metadata;
    }

    public EvidenceSections.EvidenceSummary evidenceSummary() {
        return evidenceSummary;
    }

    public List<EvidenceRecord> allEvidence() {
        return allEvidence;
    }

    /** Returns a copy whose metadata marks a cache hit (same evidence content). */
    public EvidenceBundle asCachedHit() {
        EvidenceSections.BundleMetadata cachedMeta =
                new EvidenceSections.BundleMetadata(
                        metadata.bundleId(),
                        metadata.generatedAt(),
                        metadata.engineVersion(),
                        metadata.totalEvidenceItems(),
                        metadata.averageConfidence(),
                        true);
        return new EvidenceBundle(
                investigationId,
                repositoryId,
                repositoryInformation,
                investigationTarget,
                timeline,
                ownership,
                impact,
                relationships,
                dependencies,
                packageHealth,
                hotspots,
                statistics,
                supportingRefs,
                cachedMeta,
                evidenceSummary,
                allEvidence);
    }

    /** Deduplicate by {@link EvidenceRecord#dedupeKey()} preserving insertion order. */
    public static List<EvidenceRecord> deduplicate(List<EvidenceRecord> records) {
        Map<String, EvidenceRecord> unique = new LinkedHashMap<>();
        for (EvidenceRecord record : records) {
            unique.putIfAbsent(record.dedupeKey(), record);
        }
        return Collections.unmodifiableList(new ArrayList<>(unique.values()));
    }
}
