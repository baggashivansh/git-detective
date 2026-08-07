package com.gitdetective.evidence.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Immutable sections that compose an {@link EvidenceBundle}. */
public final class EvidenceSections {

    private EvidenceSections() {}

    public record RepositoryInformation(
            UUID repositoryId,
            String name,
            String sourceType,
            String sourceUri,
            String defaultBranch,
            String primaryLanguage,
            long totalCommits,
            String analysisStatus,
            Instant analyzedAt) {}

    public record InvestigationTargetSection(
            UUID investigationId,
            String targetType,
            String targetRef,
            String targetLabel,
            String status,
            String summary,
            Instant createdAt,
            Instant completedAt) {}

    public record TimelineSection(List<EvidenceRecord> events) {
        public TimelineSection {
            events = List.copyOf(Objects.requireNonNullElse(events, List.of()));
        }
    }

    public record OwnershipSection(
            List<EvidenceRecord> owners,
            Integer busFactorScore,
            String busFactorLevel,
            String calculationNote) {
        public OwnershipSection {
            owners = List.copyOf(Objects.requireNonNullElse(owners, List.of()));
        }
    }

    public record ImpactSection(
            List<EvidenceRecord> items, BigDecimal blastRadiusScore, String calculationNote) {
        public ImpactSection {
            items = List.copyOf(Objects.requireNonNullElse(items, List.of()));
        }
    }

    public record RelationshipSection(List<EvidenceRecord> relationships) {
        public RelationshipSection {
            relationships = List.copyOf(Objects.requireNonNullElse(relationships, List.of()));
        }
    }

    public record DependencySection(List<EvidenceRecord> dependencies) {
        public DependencySection {
            dependencies = List.copyOf(Objects.requireNonNullElse(dependencies, List.of()));
        }
    }

    public record PackageHealthSection(List<EvidenceRecord> packages) {
        public PackageHealthSection {
            packages = List.copyOf(Objects.requireNonNullElse(packages, List.of()));
        }
    }

    public record HotspotSection(List<EvidenceRecord> hotspots) {
        public HotspotSection {
            hotspots = List.copyOf(Objects.requireNonNullElse(hotspots, List.of()));
        }
    }

    public record StatisticsSection(
            int evidenceCount,
            int timelineCount,
            int ownershipCount,
            int impactCount,
            int relationshipCount,
            int hotspotCount,
            int packageHealthCount,
            int clusterCount,
            int traceCount,
            List<EvidenceRecord> statisticRecords) {
        public StatisticsSection {
            statisticRecords = List.copyOf(Objects.requireNonNullElse(statisticRecords, List.of()));
        }
    }

    public record SupportingRefs(
            List<String> commits,
            List<String> files,
            List<String> packages,
            List<String> classes,
            List<String> methods,
            List<String> contributors) {
        public SupportingRefs {
            commits = List.copyOf(Objects.requireNonNullElse(commits, List.of()));
            files = List.copyOf(Objects.requireNonNullElse(files, List.of()));
            packages = List.copyOf(Objects.requireNonNullElse(packages, List.of()));
            classes = List.copyOf(Objects.requireNonNullElse(classes, List.of()));
            methods = List.copyOf(Objects.requireNonNullElse(methods, List.of()));
            contributors = List.copyOf(Objects.requireNonNullElse(contributors, List.of()));
        }

        public static SupportingRefs empty() {
            return new SupportingRefs(
                    List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }
    }

    public record BundleMetadata(
            UUID bundleId,
            Instant generatedAt,
            String engineVersion,
            int totalEvidenceItems,
            int averageConfidence,
            boolean cached) {}

    public record EvidenceSummary(
            String factualOverview,
            int verifiedCount,
            int pendingCount,
            List<String> provenanceSources) {
        public EvidenceSummary {
            provenanceSources =
                    List.copyOf(Objects.requireNonNullElse(provenanceSources, List.of()));
        }
    }
}
