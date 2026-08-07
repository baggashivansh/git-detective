package com.gitdetective.dto.response;

import com.gitdetective.entity.InvestigationRelationshipType;
import com.gitdetective.entity.OwnershipKind;
import com.gitdetective.entity.RiskLevel;
import com.gitdetective.entity.TimelineEventType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvestigationDetailResponse(
        InvestigationSummaryResponse summary,
        List<EvidenceItem> evidence,
        List<TimelineItem> timeline,
        List<OwnershipItem> ownership,
        List<ImpactItem> impact,
        List<RelationshipItem> relationships,
        List<HotspotItem> hotspots,
        List<PackageHealthItem> packageHealth,
        List<CommitClusterItem> commitClusters,
        List<TraceItem> traces) {

    public record EvidenceItem(
            UUID id,
            String evidenceType,
            String sourceKind,
            String sourceRef,
            String label,
            String detail) {}

    public record TimelineItem(
            UUID id,
            Instant occurredAt,
            TimelineEventType eventType,
            String title,
            String detail,
            String actorName,
            String actorEmail,
            String commitSha,
            String evidenceRef) {}

    public record OwnershipItem(
            UUID id,
            String contributorName,
            String contributorEmail,
            long totalCommits,
            long recentCommits,
            long linesChanged,
            BigDecimal ownershipPercentage,
            OwnershipKind ownershipKind,
            Instant lastContributionAt) {}

    public record ImpactItem(
            UUID id,
            String itemKind,
            String itemRef,
            String itemLabel,
            int dependencyDepth,
            String reason) {}

    public record RelationshipItem(
            UUID id,
            String sourceKey,
            String sourceLabel,
            String sourceType,
            String targetKey,
            String targetLabel,
            String targetType,
            InvestigationRelationshipType relationshipType,
            String evidenceRef) {}

    public record HotspotItem(
            UUID id,
            String hotspotKind,
            String itemRef,
            String itemLabel,
            BigDecimal score,
            int rankPosition,
            String detail) {}

    public record PackageHealthItem(
            UUID id,
            String packageName,
            BigDecimal complexityScore,
            int dependencyCount,
            int packageSize,
            BigDecimal modificationFrequency,
            int contributorCount,
            BigDecimal growthScore,
            RiskLevel riskLevel) {}

    public record CommitClusterItem(
            UUID id,
            String clusterLabel,
            Instant startAt,
            Instant endAt,
            int commitCount,
            int sharedFiles,
            String contributors,
            String commitShas) {}

    public record TraceItem(
            UUID id,
            String traceKind,
            int stepOrder,
            String stepLabel,
            String stepRef,
            String evidenceRef,
            String detail) {}
}
