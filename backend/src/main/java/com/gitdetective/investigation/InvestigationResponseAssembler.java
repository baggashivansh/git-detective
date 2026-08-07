package com.gitdetective.investigation;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.InvestigationEntity;
import com.gitdetective.repository.InvestigationCommitClusterJpaRepository;
import com.gitdetective.repository.InvestigationEvidenceJpaRepository;
import com.gitdetective.repository.InvestigationHotspotJpaRepository;
import com.gitdetective.repository.InvestigationImpactItemJpaRepository;
import com.gitdetective.repository.InvestigationOwnershipJpaRepository;
import com.gitdetective.repository.InvestigationPackageHealthJpaRepository;
import com.gitdetective.repository.InvestigationRelationshipJpaRepository;
import com.gitdetective.repository.InvestigationTimelineEventJpaRepository;
import com.gitdetective.repository.InvestigationTraceJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestigationResponseAssembler {

    private final InvestigationEvidenceJpaRepository evidenceJpaRepository;
    private final InvestigationTimelineEventJpaRepository timelineJpaRepository;
    private final InvestigationOwnershipJpaRepository ownershipJpaRepository;
    private final InvestigationImpactItemJpaRepository impactItemJpaRepository;
    private final InvestigationRelationshipJpaRepository relationshipJpaRepository;
    private final InvestigationHotspotJpaRepository hotspotJpaRepository;
    private final InvestigationPackageHealthJpaRepository packageHealthJpaRepository;
    private final InvestigationCommitClusterJpaRepository commitClusterJpaRepository;
    private final InvestigationTraceJpaRepository traceJpaRepository;

    public InvestigationSummaryResponse toSummary(InvestigationEntity entity) {
        return new InvestigationSummaryResponse(
                entity.getId(),
                entity.getRepositoryId(),
                entity.getTargetType(),
                entity.getTargetRef(),
                entity.getTargetLabel(),
                entity.getStatus(),
                entity.getSummary(),
                entity.getBusFactorScore(),
                entity.getBusFactorLevel(),
                entity.getBlastRadiusScore(),
                entity.getCreatedAt(),
                entity.getCompletedAt());
    }

    public InvestigationDetailResponse toDetail(InvestigationEntity entity) {
        return new InvestigationDetailResponse(
                toSummary(entity),
                evidence(entity),
                timeline(entity),
                ownership(entity),
                impact(entity),
                relationships(entity),
                hotspots(entity),
                packageHealth(entity),
                clusters(entity),
                traces(entity));
    }

    public InvestigationDetailResponse toTimelineView(InvestigationEntity entity) {
        return new InvestigationDetailResponse(
                toSummary(entity),
                List.of(),
                timeline(entity),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    public InvestigationDetailResponse toOwnershipView(InvestigationEntity entity) {
        return new InvestigationDetailResponse(
                toSummary(entity),
                List.of(),
                List.of(),
                ownership(entity),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    public InvestigationDetailResponse toImpactView(InvestigationEntity entity) {
        return new InvestigationDetailResponse(
                toSummary(entity),
                List.of(),
                List.of(),
                List.of(),
                impact(entity),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    public InvestigationDetailResponse toRelationshipsView(InvestigationEntity entity) {
        return new InvestigationDetailResponse(
                toSummary(entity),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                relationships(entity),
                List.of(),
                List.of(),
                List.of(),
                List.of());
    }

    private List<InvestigationDetailResponse.EvidenceItem> evidence(InvestigationEntity entity) {
        return evidenceJpaRepository
                .findByInvestigationIdOrderBySortOrderAsc(entity.getId())
                .stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.EvidenceItem(
                                        item.getId(),
                                        item.getEvidenceType().name(),
                                        item.getSourceKind(),
                                        item.getSourceRef(),
                                        item.getLabel(),
                                        item.getDetail()))
                .toList();
    }

    private List<InvestigationDetailResponse.TimelineItem> timeline(InvestigationEntity entity) {
        return timelineJpaRepository
                .findByInvestigationIdOrderByOccurredAtAscSortOrderAsc(entity.getId())
                .stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.TimelineItem(
                                        item.getId(),
                                        item.getOccurredAt(),
                                        item.getEventType(),
                                        item.getTitle(),
                                        item.getDetail(),
                                        item.getActorName(),
                                        item.getActorEmail(),
                                        item.getCommitSha(),
                                        item.getEvidenceRef()))
                .toList();
    }

    private List<InvestigationDetailResponse.OwnershipItem> ownership(InvestigationEntity entity) {
        return ownershipJpaRepository
                .findByInvestigationIdOrderByOwnershipPercentageDesc(entity.getId())
                .stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.OwnershipItem(
                                        item.getId(),
                                        item.getContributorName(),
                                        item.getContributorEmail(),
                                        item.getTotalCommits(),
                                        item.getRecentCommits(),
                                        item.getLinesChanged(),
                                        item.getOwnershipPercentage(),
                                        item.getOwnershipKind(),
                                        item.getLastContributionAt()))
                .toList();
    }

    private List<InvestigationDetailResponse.ImpactItem> impact(InvestigationEntity entity) {
        return impactItemJpaRepository
                .findByInvestigationIdOrderByDependencyDepthAsc(entity.getId())
                .stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.ImpactItem(
                                        item.getId(),
                                        item.getItemKind(),
                                        item.getItemRef(),
                                        item.getItemLabel(),
                                        item.getDependencyDepth(),
                                        item.getReason()))
                .toList();
    }

    private List<InvestigationDetailResponse.RelationshipItem> relationships(
            InvestigationEntity entity) {
        return relationshipJpaRepository.findByInvestigationId(entity.getId()).stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.RelationshipItem(
                                        item.getId(),
                                        item.getSourceKey(),
                                        item.getSourceLabel(),
                                        item.getSourceType(),
                                        item.getTargetKey(),
                                        item.getTargetLabel(),
                                        item.getTargetType(),
                                        item.getRelationshipType(),
                                        item.getEvidenceRef()))
                .toList();
    }

    private List<InvestigationDetailResponse.HotspotItem> hotspots(InvestigationEntity entity) {
        return hotspotJpaRepository
                .findByInvestigationIdOrderByRankPositionAsc(entity.getId())
                .stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.HotspotItem(
                                        item.getId(),
                                        item.getHotspotKind(),
                                        item.getItemRef(),
                                        item.getItemLabel(),
                                        item.getScore(),
                                        item.getRankPosition(),
                                        item.getDetail()))
                .toList();
    }

    private List<InvestigationDetailResponse.PackageHealthItem> packageHealth(
            InvestigationEntity entity) {
        return packageHealthJpaRepository
                .findByInvestigationIdOrderByRiskLevelDesc(entity.getId())
                .stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.PackageHealthItem(
                                        item.getId(),
                                        item.getPackageName(),
                                        item.getComplexityScore(),
                                        item.getDependencyCount(),
                                        item.getPackageSize(),
                                        item.getModificationFrequency(),
                                        item.getContributorCount(),
                                        item.getGrowthScore(),
                                        item.getRiskLevel()))
                .toList();
    }

    private List<InvestigationDetailResponse.CommitClusterItem> clusters(
            InvestigationEntity entity) {
        return commitClusterJpaRepository.findByInvestigationId(entity.getId()).stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.CommitClusterItem(
                                        item.getId(),
                                        item.getClusterLabel(),
                                        item.getStartAt(),
                                        item.getEndAt(),
                                        item.getCommitCount(),
                                        item.getSharedFiles(),
                                        item.getContributors(),
                                        item.getCommitShas()))
                .toList();
    }

    private List<InvestigationDetailResponse.TraceItem> traces(InvestigationEntity entity) {
        return traceJpaRepository.findByInvestigationIdOrderByStepOrderAsc(entity.getId()).stream()
                .map(
                        item ->
                                new InvestigationDetailResponse.TraceItem(
                                        item.getId(),
                                        item.getTraceKind(),
                                        item.getStepOrder(),
                                        item.getStepLabel(),
                                        item.getStepRef(),
                                        item.getEvidenceRef(),
                                        item.getDetail()))
                .toList();
    }
}
