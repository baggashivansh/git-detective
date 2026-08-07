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
public class PackageHealthCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "PackageHealthCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now = Instant.now();
        for (InvestigationDetailResponse.PackageHealthItem item : detail.packageHealth()) {
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.PACKAGE_HEALTH)
                            .source(EvidenceProvenance.PACKAGE_HEALTH_ENGINE)
                            .sourceIdentifier(mapper.requireRef(item.packageName(), "package"))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(now)
                            .description(
                                    "Package health "
                                            + mapper.nullToEmpty(item.packageName())
                                            + " risk="
                                            + item.riskLevel()
                                            + " complexity="
                                            + item.complexityScore()
                                            + " deps="
                                            + item.dependencyCount())
                            .meta("complexityScore", String.valueOf(item.complexityScore()))
                            .meta("dependencyCount", String.valueOf(item.dependencyCount()))
                            .meta("packageSize", String.valueOf(item.packageSize()))
                            .meta(
                                    "modificationFrequency",
                                    String.valueOf(item.modificationFrequency()))
                            .meta("contributorCount", String.valueOf(item.contributorCount()))
                            .meta("growthScore", String.valueOf(item.growthScore()))
                            .meta(
                                    "riskLevel",
                                    item.riskLevel() == null ? "" : item.riskLevel().name())
                            .build());
        }
        return records;
    }
}
