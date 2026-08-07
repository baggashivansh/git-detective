package com.gitdetective.assistant.context;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.evidence.builder.EvidenceBundleBuilder;
import com.gitdetective.evidence.collector.OwnershipCollector;
import com.gitdetective.evidence.collector.RepositoryMetadataCollector;
import com.gitdetective.evidence.collector.StatisticsCollector;
import com.gitdetective.evidence.collector.TimelineCollector;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.validator.EvidenceValidator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EvidenceContextBuilderTest {

    @Test
    @DisplayName("builds compact ownership-prioritized context from evidence bundle")
    void buildsContext() {
        InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();
        EvidenceBundle bundle =
                new EvidenceBundleBuilder(
                                List.of(
                                        new RepositoryMetadataCollector(mapper),
                                        new TimelineCollector(mapper),
                                        new OwnershipCollector(mapper),
                                        new StatisticsCollector(mapper)),
                                new EvidenceValidator(),
                                mapper,
                                "1.0.0")
                        .build(
                                EvidenceTestFixtures.completedDetail(),
                                EvidenceTestFixtures.repository());

        EvidenceContextBuilder.EvidenceContext context =
                new EvidenceContextBuilder().build(bundle, AssistantIntent.OWNERSHIP);

        assertThat(context.compactEvidence()).isNotBlank();
        assertThat(context.selectedEvidence()).isNotEmpty();
        assertThat(context.averageConfidence()).isGreaterThan(0);
    }
}
