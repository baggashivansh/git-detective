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
public class ImpactCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "ImpactCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now = Instant.now();
        for (InvestigationDetailResponse.ImpactItem item : detail.impact()) {
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.IMPACT)
                            .source(EvidenceProvenance.IMPACT_ENGINE)
                            .sourceIdentifier(mapper.requireRef(item.itemRef(), item.itemLabel()))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(now)
                            .description(
                                    "Impact "
                                            + mapper.nullToEmpty(item.itemKind())
                                            + " "
                                            + mapper.nullToEmpty(item.itemLabel())
                                            + " depth="
                                            + item.dependencyDepth()
                                            + (item.reason() == null || item.reason().isBlank()
                                                    ? ""
                                                    : " reason=" + item.reason()))
                            .meta("itemKind", mapper.nullToEmpty(item.itemKind()))
                            .meta("dependencyDepth", String.valueOf(item.dependencyDepth()))
                            .meta("reason", mapper.nullToEmpty(item.reason()))
                            .build());
        }
        return records;
    }
}
