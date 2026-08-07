package com.gitdetective.evidence.collector;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatisticsCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "StatisticsCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        Instant now = Instant.now();
        UUID investigationId = detail.summary().id();
        UUID repositoryId = detail.summary().repositoryId();
        List<EvidenceRecord> records = new ArrayList<>();

        records.add(
                stat(
                        investigationId,
                        repositoryId,
                        now,
                        "timelineCount",
                        detail.timeline().size()));
        records.add(
                stat(
                        investigationId,
                        repositoryId,
                        now,
                        "ownershipCount",
                        detail.ownership().size()));
        records.add(
                stat(investigationId, repositoryId, now, "impactCount", detail.impact().size()));
        records.add(
                stat(
                        investigationId,
                        repositoryId,
                        now,
                        "relationshipCount",
                        detail.relationships().size()));
        records.add(
                stat(investigationId, repositoryId, now, "hotspotCount", detail.hotspots().size()));
        records.add(
                stat(
                        investigationId,
                        repositoryId,
                        now,
                        "packageHealthCount",
                        detail.packageHealth().size()));
        records.add(
                stat(
                        investigationId,
                        repositoryId,
                        now,
                        "clusterCount",
                        detail.commitClusters().size()));
        records.add(stat(investigationId, repositoryId, now, "traceCount", detail.traces().size()));
        records.add(
                stat(
                        investigationId,
                        repositoryId,
                        now,
                        "rawEvidenceCount",
                        detail.evidence().size()));

        if (detail.summary().busFactorScore() != null) {
            records.add(
                    EvidenceRecord.builder()
                            .evidenceType(EvidenceCategory.STATISTIC)
                            .source(EvidenceProvenance.STATISTICS_COLLECTOR)
                            .sourceIdentifier("busFactorScore")
                            .repositoryId(repositoryId)
                            .investigationId(investigationId)
                            .timestamp(now)
                            .description(
                                    "Bus factor score="
                                            + detail.summary().busFactorScore()
                                            + " level="
                                            + detail.summary().busFactorLevel())
                            .meta(
                                    "busFactorScore",
                                    String.valueOf(detail.summary().busFactorScore()))
                            .meta(
                                    "busFactorLevel",
                                    detail.summary().busFactorLevel() == null
                                            ? ""
                                            : detail.summary().busFactorLevel().name())
                            .build());
        }
        if (detail.summary().blastRadiusScore() != null) {
            records.add(
                    EvidenceRecord.builder()
                            .evidenceType(EvidenceCategory.STATISTIC)
                            .source(EvidenceProvenance.STATISTICS_COLLECTOR)
                            .sourceIdentifier("blastRadiusScore")
                            .repositoryId(repositoryId)
                            .investigationId(investigationId)
                            .timestamp(now)
                            .description(
                                    "Blast radius score=" + detail.summary().blastRadiusScore())
                            .meta(
                                    "blastRadiusScore",
                                    String.valueOf(detail.summary().blastRadiusScore()))
                            .build());
        }
        return records;
    }

    private static EvidenceRecord stat(
            UUID investigationId, UUID repositoryId, Instant now, String key, int value) {
        return EvidenceRecord.builder()
                .evidenceType(EvidenceCategory.STATISTIC)
                .source(EvidenceProvenance.STATISTICS_COLLECTOR)
                .sourceIdentifier(key)
                .repositoryId(repositoryId)
                .investigationId(investigationId)
                .timestamp(now)
                .description(key + "=" + value)
                .meta(key, String.valueOf(value))
                .build();
    }
}
