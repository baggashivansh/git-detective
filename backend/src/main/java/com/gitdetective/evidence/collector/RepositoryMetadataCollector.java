package com.gitdetective.evidence.collector;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RepositoryMetadataCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "RepositoryMetadataCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        Instant analyzedAt =
                repository.getAnalyzedAt() != null ? repository.getAnalyzedAt() : Instant.now();
        return List.of(
                EvidenceRecord.builder()
                        .evidenceType(EvidenceCategory.REPOSITORY)
                        .source(EvidenceProvenance.REPOSITORY_METADATA)
                        .sourceIdentifier(repository.getId().toString())
                        .repositoryId(repository.getId())
                        .investigationId(detail.summary().id())
                        .timestamp(analyzedAt)
                        .description(
                                "Repository "
                                        + repository.getName()
                                        + " source="
                                        + repository.getSourceType()
                                        + " status="
                                        + repository.getStatus()
                                        + " commits="
                                        + repository.getTotalCommits())
                        .meta("name", repository.getName())
                        .meta("sourceType", repository.getSourceType().name())
                        .meta("sourceUri", repository.getSourceUri())
                        .meta("defaultBranch", mapper.nullToEmpty(repository.getDefaultBranch()))
                        .meta(
                                "primaryLanguage",
                                mapper.nullToEmpty(repository.getPrimaryLanguage()))
                        .meta("analysisStatus", repository.getStatus().name())
                        .build(),
                EvidenceRecord.builder()
                        .evidenceType(EvidenceCategory.TARGET)
                        .source(EvidenceProvenance.INVESTIGATION_TARGET)
                        .sourceIdentifier(
                                mapper.requireRef(
                                        detail.summary().targetRef(),
                                        detail.summary().targetLabel()))
                        .repositoryId(detail.summary().repositoryId())
                        .investigationId(detail.summary().id())
                        .timestamp(
                                detail.summary().createdAt() != null
                                        ? detail.summary().createdAt()
                                        : Instant.now())
                        .description(
                                "Investigation target "
                                        + detail.summary().targetType()
                                        + " "
                                        + mapper.nullToEmpty(detail.summary().targetLabel()))
                        .meta("targetType", detail.summary().targetType().name())
                        .meta("targetRef", mapper.nullToEmpty(detail.summary().targetRef()))
                        .meta("status", detail.summary().status().name())
                        .build());
    }
}
