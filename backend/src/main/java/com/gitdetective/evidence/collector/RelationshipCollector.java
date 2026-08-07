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
public class RelationshipCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "RelationshipCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now = Instant.now();
        for (InvestigationDetailResponse.RelationshipItem item : detail.relationships()) {
            String id =
                    mapper.requireRef(item.sourceKey(), "src")
                            + "->"
                            + (item.relationshipType() == null
                                    ? "REL"
                                    : item.relationshipType().name())
                            + "->"
                            + mapper.requireRef(item.targetKey(), "tgt");
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.RELATIONSHIP)
                            .source(EvidenceProvenance.RELATIONSHIP_ENGINE)
                            .sourceIdentifier(id)
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(now)
                            .description(
                                    mapper.nullToEmpty(item.sourceLabel())
                                            + " "
                                            + (item.relationshipType() == null
                                                    ? "RELATED_TO"
                                                    : item.relationshipType().name())
                                            + " "
                                            + mapper.nullToEmpty(item.targetLabel()))
                            .meta("sourceType", mapper.nullToEmpty(item.sourceType()))
                            .meta("targetType", mapper.nullToEmpty(item.targetType()))
                            .meta("evidenceRef", mapper.nullToEmpty(item.evidenceRef()))
                            .build());
        }
        return records;
    }
}
