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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimelineCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "TimelineCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant fallback = Instant.now();
        for (InvestigationDetailResponse.TimelineItem item : detail.timeline()) {
            Instant ts = item.occurredAt() != null ? item.occurredAt() : fallback;
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : java.util.UUID.randomUUID())
                            .evidenceType(EvidenceCategory.TIMELINE)
                            .source(EvidenceProvenance.TIMELINE_ENGINE)
                            .sourceIdentifier(
                                    mapper.requireRef(
                                            item.commitSha(),
                                            mapper.requireRef(item.evidenceRef(), item.title())))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(ts)
                            .description(
                                    item.eventType()
                                            + ": "
                                            + mapper.nullToEmpty(item.title())
                                            + (item.detail() == null || item.detail().isBlank()
                                                    ? ""
                                                    : " — " + item.detail()))
                            .meta("eventType", String.valueOf(item.eventType()))
                            .meta("actorEmail", mapper.nullToEmpty(item.actorEmail()))
                            .meta("commitSha", mapper.nullToEmpty(item.commitSha()))
                            .build());
        }
        return records;
    }
}
