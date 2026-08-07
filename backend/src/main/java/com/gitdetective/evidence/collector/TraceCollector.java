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
public class TraceCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "TraceCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now = Instant.now();
        for (InvestigationDetailResponse.TraceItem item : detail.traces()) {
            EvidenceProvenance provenance =
                    item.traceKind() != null && item.traceKind().toUpperCase().contains("AUTH")
                            ? EvidenceProvenance.AUTH_FLOW_DETECTOR
                            : EvidenceProvenance.REQUEST_TRACE_ENGINE;
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.TRACE)
                            .source(provenance)
                            .sourceIdentifier(
                                    mapper.requireRef(
                                            item.stepRef(),
                                            mapper.requireRef(
                                                    item.evidenceRef(), item.stepLabel())))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(now)
                            .description(
                                    mapper.nullToEmpty(item.traceKind())
                                            + " step="
                                            + item.stepOrder()
                                            + " "
                                            + mapper.nullToEmpty(item.stepLabel()))
                            .meta("traceKind", mapper.nullToEmpty(item.traceKind()))
                            .meta("stepOrder", String.valueOf(item.stepOrder()))
                            .meta("evidenceRef", mapper.nullToEmpty(item.evidenceRef()))
                            .meta("detail", mapper.nullToEmpty(item.detail()))
                            .build());
        }
        return records;
    }
}
