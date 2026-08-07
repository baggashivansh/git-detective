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
public class OwnershipCollector implements EvidenceCollector {

    private final InvestigationEvidenceMapper mapper;

    @Override
    public String name() {
        return "OwnershipCollector";
    }

    @Override
    public List<EvidenceRecord> collect(
            InvestigationDetailResponse detail, CodeRepository repository) {
        mapper.requireDetail(detail);
        List<EvidenceRecord> records = new ArrayList<>();
        Instant now = Instant.now();
        for (InvestigationDetailResponse.OwnershipItem item : detail.ownership()) {
            Instant ts = item.lastContributionAt() != null ? item.lastContributionAt() : now;
            records.add(
                    EvidenceRecord.builder()
                            .evidenceId(item.id() != null ? item.id() : UUID.randomUUID())
                            .evidenceType(EvidenceCategory.OWNERSHIP)
                            .source(EvidenceProvenance.OWNERSHIP_ENGINE)
                            .sourceIdentifier(
                                    mapper.requireRef(
                                            item.contributorEmail(), item.contributorName()))
                            .repositoryId(detail.summary().repositoryId())
                            .investigationId(detail.summary().id())
                            .timestamp(ts)
                            .description(
                                    "Owner "
                                            + mapper.nullToEmpty(item.contributorName())
                                            + " <"
                                            + mapper.nullToEmpty(item.contributorEmail())
                                            + "> ownership="
                                            + item.ownershipPercentage()
                                            + "% kind="
                                            + item.ownershipKind())
                            .meta("totalCommits", String.valueOf(item.totalCommits()))
                            .meta("recentCommits", String.valueOf(item.recentCommits()))
                            .meta("linesChanged", String.valueOf(item.linesChanged()))
                            .meta("ownershipPercentage", String.valueOf(item.ownershipPercentage()))
                            .meta(
                                    "ownershipKind",
                                    item.ownershipKind() == null ? "" : item.ownershipKind().name())
                            .build());
        }
        return records;
    }
}
