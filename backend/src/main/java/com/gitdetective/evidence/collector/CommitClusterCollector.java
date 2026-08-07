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
public class CommitClusterCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "CommitClusterCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        for (InvestigationDetailResponse.CommitClusterItem item : detail.commitClusters()) {
            Instant ts = item.startAt() != null ? item.startAt() : Instant.now();
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.CLUSTER)
                            .source(EvidenceProvenance.COMMIT_CLUSTERING_ENGINE)
                            .sourceIdentifier(
                                    mapper.requireRef(
                                            item.clusterLabel(), String.valueOf(item.id())))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(ts)
                            .description(
                                    "Cluster "
                                            + mapper.nullToEmpty(item.clusterLabel())
                                            + " commits="
                                            + item.commitCount()
                                            + " sharedFiles="
                                            + item.sharedFiles())
                            .meta("commitCount", String.valueOf(item.commitCount()))
                            .meta("sharedFiles", String.valueOf(item.sharedFiles()))
                            .meta("contributors", mapper.nullToEmpty(item.contributors()))
                            .meta("commitShas", mapper.nullToEmpty(item.commitShas()))
                            .build());
        }
        return records;
    }
}
