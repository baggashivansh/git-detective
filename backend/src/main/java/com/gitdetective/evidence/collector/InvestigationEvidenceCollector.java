package com.gitdetective.evidence.collector;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Normalizes persisted investigation evidence rows into the evidence abstraction. */
@Component
@RequiredArgsConstructor
public class InvestigationEvidenceCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "InvestigationEvidenceCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now =
                detail.summary().completedAt() != null
                        ? detail.summary().completedAt()
                        : Instant.now();
        for (InvestigationDetailResponse.EvidenceItem item : detail.evidence()) {
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(mapper.mapEvidenceType(item.evidenceType()))
                            .source(EvidenceProvenance.INVESTIGATION_EVIDENCE)
                            .sourceIdentifier(mapper.requireRef(item.sourceRef(), item.label()))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(now)
                            .description(
                                    mapper.nullToEmpty(item.label())
                                            + (item.detail() == null || item.detail().isBlank()
                                                    ? ""
                                                    : " — " + item.detail()))
                            .meta("sourceKind", mapper.nullToEmpty(item.sourceKind()))
                            .meta("rawEvidenceType", mapper.nullToEmpty(item.evidenceType()))
                            .build());
        }
        return records;
    }
}
