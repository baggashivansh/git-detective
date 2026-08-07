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
public class HotspotCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "HotspotCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now = Instant.now();
        for (InvestigationDetailResponse.HotspotItem item : detail.hotspots()) {
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.HOTSPOT)
                            .source(EvidenceProvenance.HOTSPOT_DETECTOR)
                            .sourceIdentifier(mapper.requireRef(item.itemRef(), item.itemLabel()))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(now)
                            .description(
                                    "Hotspot rank="
                                            + item.rankPosition()
                                            + " kind="
                                            + mapper.nullToEmpty(item.hotspotKind())
                                            + " "
                                            + mapper.nullToEmpty(item.itemLabel())
                                            + " score="
                                            + item.score())
                            .meta("hotspotKind", mapper.nullToEmpty(item.hotspotKind()))
                            .meta("score", String.valueOf(item.score()))
                            .meta("rankPosition", String.valueOf(item.rankPosition()))
                            .meta("detail", mapper.nullToEmpty(item.detail()))
                            .build());
        }
        return records;
    }
}
