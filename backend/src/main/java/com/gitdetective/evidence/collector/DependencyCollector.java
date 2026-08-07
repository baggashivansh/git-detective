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

/**
 * Collects dependency-oriented evidence from impact items and raw investigation evidence rows of
 * type DEPENDENCY / IMPORT.
 */
@Component
@RequiredArgsConstructor
public class DependencyCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "DependencyCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now = Instant.now();

        for (InvestigationDetailResponse.EvidenceItem item : detail.evidence()) {
            String type = item.evidenceType() == null ? "" : item.evidenceType().toUpperCase();
            if (!"DEPENDENCY".equals(type) && !"IMPORT".equals(type)) {
                continue;
            }
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(
                                    "IMPORT".equals(type)
                                            ? EvidenceCategory.IMPORT
                                            : EvidenceCategory.DEPENDENCY)
                            .source(EvidenceProvenance.DEPENDENCY_GRAPH)
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
                            .build());
        }

        for (InvestigationDetailResponse.ImpactItem item : detail.impact()) {
            if (item.itemKind() == null || !item.itemKind().toUpperCase().contains("DEPEND")) {
                continue;
            }
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.DEPENDENCY)
                            .source(EvidenceProvenance.DEPENDENCY_GRAPH)
                            .sourceIdentifier(mapper.requireRef(item.itemRef(), item.itemLabel()))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(now)
                            .description(
                                    "Dependency impact "
                                            + mapper.nullToEmpty(item.itemLabel())
                                            + " depth="
                                            + item.dependencyDepth())
                            .meta("itemKind", mapper.nullToEmpty(item.itemKind()))
                            .build());
        }
        return records;
    }
}
