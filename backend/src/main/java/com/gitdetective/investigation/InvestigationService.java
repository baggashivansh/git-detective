package com.gitdetective.investigation;

import com.gitdetective.dto.request.CreateInvestigationRequest;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationReportResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.EvidenceType;
import com.gitdetective.entity.InvestigationCommitClusterEntity;
import com.gitdetective.entity.InvestigationEntity;
import com.gitdetective.entity.InvestigationEvidenceEntity;
import com.gitdetective.entity.InvestigationHotspotEntity;
import com.gitdetective.entity.InvestigationImpactItemEntity;
import com.gitdetective.entity.InvestigationOwnershipEntity;
import com.gitdetective.entity.InvestigationPackageHealthEntity;
import com.gitdetective.entity.InvestigationRelationshipEntity;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTimelineEventEntity;
import com.gitdetective.entity.InvestigationTraceEntity;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.exception.ResourceNotFoundException;
import com.gitdetective.history.FileHistoryEngine;
import com.gitdetective.impact.ImpactEngine;
import com.gitdetective.ownership.OwnershipEngine;
import com.gitdetective.relationship.RelationshipEngine;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import com.gitdetective.repository.InvestigationCommitClusterJpaRepository;
import com.gitdetective.repository.InvestigationEvidenceJpaRepository;
import com.gitdetective.repository.InvestigationHotspotJpaRepository;
import com.gitdetective.repository.InvestigationImpactItemJpaRepository;
import com.gitdetective.repository.InvestigationJpaRepository;
import com.gitdetective.repository.InvestigationOwnershipJpaRepository;
import com.gitdetective.repository.InvestigationPackageHealthJpaRepository;
import com.gitdetective.repository.InvestigationRelationshipJpaRepository;
import com.gitdetective.repository.InvestigationTimelineEventJpaRepository;
import com.gitdetective.repository.InvestigationTraceJpaRepository;
import com.gitdetective.timeline.TimelineEngine;
import com.gitdetective.trace.AuthFlowDetector;
import com.gitdetective.trace.RequestTraceEngine;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestigationService {

    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final InvestigationJpaRepository investigationJpaRepository;
    private final InvestigationEvidenceJpaRepository evidenceJpaRepository;
    private final InvestigationTimelineEventJpaRepository timelineJpaRepository;
    private final InvestigationRelationshipJpaRepository relationshipJpaRepository;
    private final InvestigationOwnershipJpaRepository ownershipJpaRepository;
    private final InvestigationImpactItemJpaRepository impactItemJpaRepository;
    private final InvestigationHotspotJpaRepository hotspotJpaRepository;
    private final InvestigationPackageHealthJpaRepository packageHealthJpaRepository;
    private final InvestigationCommitClusterJpaRepository commitClusterJpaRepository;
    private final InvestigationTraceJpaRepository traceJpaRepository;
    private final InvestigationTargetResolver targetResolver;
    private final TimelineEngine timelineEngine;
    private final OwnershipEngine ownershipEngine;
    private final ImpactEngine impactEngine;
    private final RelationshipEngine relationshipEngine;
    private final FileHistoryEngine fileHistoryEngine;
    private final RequestTraceEngine requestTraceEngine;
    private final AuthFlowDetector authFlowDetector;
    private final HotspotDetector hotspotDetector;
    private final PackageHealthEngine packageHealthEngine;
    private final CommitClusteringEngine commitClusteringEngine;
    private final InvestigationResponseAssembler assembler;
    private final InvestigationReportExporter reportExporter;

    @Transactional
    public InvestigationSummaryResponse create(CreateInvestigationRequest request) {
        CodeRepository repository =
                codeRepositoryJpaRepository
                        .findById(request.repositoryId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Repository not found: " + request.repositoryId()));
        if (repository.getStatus() != AnalysisStatus.COMPLETED) {
            throw new RepositoryAnalysisException(
                    HttpStatus.CONFLICT,
                    "REPOSITORY_NOT_READY",
                    "Repository analysis must be COMPLETED before investigation");
        }

        InvestigationEntity investigation =
                investigationJpaRepository.save(
                        InvestigationEntity.builder()
                                .repositoryId(repository.getId())
                                .targetType(request.targetType())
                                .targetRef(request.targetRef())
                                .targetLabel(request.targetRef())
                                .status(InvestigationStatus.RUNNING)
                                .build());

        try {
            InvestigationTarget target =
                    targetResolver.resolve(
                            repository.getId(), request.targetType(), request.targetRef());
            investigation.setTargetLabel(target.label());
            investigation.setTargetRef(target.ref());

            List<InvestigationEvidenceEntity> evidence = new ArrayList<>();
            evidence.add(
                    evidence(
                            investigation.getId(),
                            EvidenceType.FILE,
                            target.type().name(),
                            target.ref(),
                            "Investigation target",
                            target.label(),
                            0));

            var timeline = timelineEngine.build(target);
            int order = 0;
            for (var event : timeline) {
                timelineJpaRepository.save(
                        InvestigationTimelineEventEntity.builder()
                                .investigationId(investigation.getId())
                                .occurredAt(
                                        event.occurredAt() == null
                                                ? Instant.EPOCH
                                                : event.occurredAt())
                                .eventType(event.eventType())
                                .title(event.title())
                                .detail(event.detail())
                                .actorName(event.actorName())
                                .actorEmail(event.actorEmail())
                                .commitSha(event.commitSha())
                                .evidenceRef(event.evidenceRef())
                                .sortOrder(order++)
                                .build());
                evidence.add(
                        evidence(
                                investigation.getId(),
                                EvidenceType.COMMIT,
                                "TIMELINE",
                                event.evidenceRef(),
                                event.title(),
                                event.detail(),
                                evidence.size()));
            }

            var ownership = ownershipEngine.calculate(target);
            for (var owner : ownership.owners()) {
                ownershipJpaRepository.save(
                        InvestigationOwnershipEntity.builder()
                                .investigationId(investigation.getId())
                                .contributorEmail(owner.email())
                                .contributorName(owner.name())
                                .totalCommits(owner.totalCommits())
                                .recentCommits(owner.recentCommits())
                                .linesChanged(owner.linesChanged())
                                .ownershipPercentage(owner.ownershipPercentage())
                                .ownershipKind(owner.ownershipKind())
                                .lastContributionAt(owner.lastContributionAt())
                                .build());
                evidence.add(
                        evidence(
                                investigation.getId(),
                                EvidenceType.CONTRIBUTOR,
                                "OWNERSHIP",
                                owner.email(),
                                owner.name(),
                                "ownership%=" + owner.ownershipPercentage(),
                                evidence.size()));
            }
            investigation.setBusFactorScore(ownership.busFactorScore());
            investigation.setBusFactorLevel(ownership.busFactorLevel());

            var impact = impactEngine.analyze(target);
            for (var item : impact.items()) {
                impactItemJpaRepository.save(
                        InvestigationImpactItemEntity.builder()
                                .investigationId(investigation.getId())
                                .itemKind(item.itemKind())
                                .itemRef(item.itemRef())
                                .itemLabel(item.itemLabel())
                                .dependencyDepth(item.dependencyDepth())
                                .reason(item.reason())
                                .build());
            }
            for (String item : impact.evidence()) {
                evidence.add(
                        evidence(
                                investigation.getId(),
                                EvidenceType.DEPENDENCY,
                                "IMPACT",
                                item,
                                "Impact evidence",
                                item,
                                evidence.size()));
            }
            investigation.setBlastRadiusScore(impact.blastRadiusScore());

            for (var relationship : relationshipEngine.build(target)) {
                relationshipJpaRepository.save(
                        InvestigationRelationshipEntity.builder()
                                .investigationId(investigation.getId())
                                .sourceKey(relationship.sourceKey())
                                .sourceLabel(relationship.sourceLabel())
                                .sourceType(relationship.sourceType())
                                .targetKey(relationship.targetKey())
                                .targetLabel(relationship.targetLabel())
                                .targetType(relationship.targetType())
                                .relationshipType(relationship.relationshipType())
                                .evidenceRef(relationship.evidenceRef())
                                .build());
            }

            int step = 1;
            for (var trace : requestTraceEngine.discover(target)) {
                traceJpaRepository.save(
                        InvestigationTraceEntity.builder()
                                .investigationId(investigation.getId())
                                .traceKind("REQUEST_FLOW")
                                .stepOrder(step++)
                                .stepLabel(trace.stepLabel())
                                .stepRef(trace.stepRef())
                                .evidenceRef(trace.evidenceRef())
                                .detail(trace.detail())
                                .build());
            }
            for (var auth : authFlowDetector.detect(target)) {
                traceJpaRepository.save(
                        InvestigationTraceEntity.builder()
                                .investigationId(investigation.getId())
                                .traceKind("AUTH_FLOW")
                                .stepOrder(auth.stepOrder())
                                .stepLabel(auth.stepLabel())
                                .stepRef(auth.stepRef())
                                .evidenceRef(auth.evidenceRef())
                                .detail(auth.detail())
                                .build());
            }

            for (var hotspot : hotspotDetector.detect(repository.getId())) {
                hotspotJpaRepository.save(
                        InvestigationHotspotEntity.builder()
                                .investigationId(investigation.getId())
                                .hotspotKind(hotspot.kind())
                                .itemRef(hotspot.itemRef())
                                .itemLabel(hotspot.itemLabel())
                                .score(hotspot.score())
                                .rankPosition(hotspot.rank())
                                .detail(hotspot.detail())
                                .build());
            }
            for (var health : packageHealthEngine.calculate(repository.getId())) {
                packageHealthJpaRepository.save(
                        InvestigationPackageHealthEntity.builder()
                                .investigationId(investigation.getId())
                                .packageName(health.packageName())
                                .complexityScore(health.complexityScore())
                                .dependencyCount(health.dependencyCount())
                                .packageSize(health.packageSize())
                                .modificationFrequency(health.modificationFrequency())
                                .contributorCount(health.contributorCount())
                                .growthScore(health.growthScore())
                                .riskLevel(health.riskLevel())
                                .build());
            }
            for (var cluster : commitClusteringEngine.cluster(repository.getId())) {
                commitClusterJpaRepository.save(
                        InvestigationCommitClusterEntity.builder()
                                .investigationId(investigation.getId())
                                .clusterLabel(cluster.label())
                                .startAt(cluster.startAt())
                                .endAt(cluster.endAt())
                                .commitCount(cluster.commitCount())
                                .sharedFiles(cluster.sharedFiles())
                                .contributors(cluster.contributors())
                                .commitShas(cluster.commitShas())
                                .build());
            }

            if (target.filePath() != null) {
                var history = fileHistoryEngine.analyze(repository.getId(), target.filePath());
                evidence.add(
                        evidence(
                                investigation.getId(),
                                EvidenceType.FILE,
                                "FILE_HISTORY",
                                target.filePath(),
                                "File history",
                                "modifications="
                                        + history.modificationCount()
                                        + ", authors="
                                        + history.authorCount(),
                                evidence.size()));
            }

            evidenceJpaRepository.saveAll(evidence);

            String summary =
                    "Deterministic investigation of "
                            + target.type()
                            + " '"
                            + target.label()
                            + "' on repository '"
                            + repository.getName()
                            + "'. Evidence items="
                            + evidence.size()
                            + ", timeline events="
                            + timeline.size()
                            + ", owners="
                            + ownership.owners().size()
                            + ", busFactor="
                            + ownership.busFactorScore()
                            + " ("
                            + ownership.busFactorLevel()
                            + "), blastRadius="
                            + impact.blastRadiusScore()
                            + ". "
                            + ownership.busFactorExplanation();

            investigation.setSummary(summary);
            investigation.setStatus(InvestigationStatus.COMPLETED);
            investigation.setCompletedAt(Instant.now());
            investigationJpaRepository.save(investigation);
            log.info(
                    "Investigation completed id={} repositoryId={} target={}",
                    investigation.getId(),
                    repository.getId(),
                    target.label());
            return assembler.toSummary(investigation);
        } catch (RuntimeException exception) {
            investigation.setStatus(InvestigationStatus.FAILED);
            investigation.setSummary(exception.getMessage());
            investigationJpaRepository.save(investigation);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<InvestigationSummaryResponse> list() {
        return investigationJpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(assembler::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvestigationDetailResponse get(UUID id) {
        return assembler.toDetail(require(id));
    }

    @Transactional(readOnly = true)
    public InvestigationDetailResponse timeline(UUID id) {
        return assembler.toTimelineView(require(id));
    }

    @Transactional(readOnly = true)
    public InvestigationDetailResponse ownership(UUID id) {
        return assembler.toOwnershipView(require(id));
    }

    @Transactional(readOnly = true)
    public InvestigationDetailResponse impact(UUID id) {
        return assembler.toImpactView(require(id));
    }

    @Transactional(readOnly = true)
    public InvestigationDetailResponse relationships(UUID id) {
        return assembler.toRelationshipsView(require(id));
    }

    @Transactional(readOnly = true)
    public InvestigationReportResponse report(UUID id, String format) {
        InvestigationEntity investigation = require(id);
        InvestigationDetailResponse detail = assembler.toDetail(investigation);
        return reportExporter.export(detail, format == null ? "json" : format);
    }

    private InvestigationEntity require(UUID id) {
        return investigationJpaRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investigation not found: " + id));
    }

    private InvestigationEvidenceEntity evidence(
            UUID investigationId,
            EvidenceType type,
            String sourceKind,
            String sourceRef,
            String label,
            String detail,
            int order) {
        return InvestigationEvidenceEntity.builder()
                .investigationId(investigationId)
                .evidenceType(type)
                .sourceKind(sourceKind)
                .sourceRef(sourceRef == null ? "" : sourceRef)
                .label(label)
                .detail(detail)
                .sortOrder(order)
                .build();
    }
}
