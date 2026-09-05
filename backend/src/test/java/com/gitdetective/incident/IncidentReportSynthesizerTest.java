package com.gitdetective.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.assistant.context.EvidenceContextBuilder;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.intent.IntentDetector;
import com.gitdetective.assistant.prompt.PromptBuilder;
import com.gitdetective.assistant.provider.AiProvider;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator;
import com.gitdetective.dto.response.IncidentReportResponse;
import com.gitdetective.evidence.EvidenceEngine;
import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.evidence.builder.EvidenceBundleBuilder;
import com.gitdetective.evidence.collector.CommitClusterCollector;
import com.gitdetective.evidence.collector.DependencyCollector;
import com.gitdetective.evidence.collector.HotspotCollector;
import com.gitdetective.evidence.collector.ImpactCollector;
import com.gitdetective.evidence.collector.InvestigationEvidenceCollector;
import com.gitdetective.evidence.collector.OwnershipCollector;
import com.gitdetective.evidence.collector.PackageHealthCollector;
import com.gitdetective.evidence.collector.RelationshipCollector;
import com.gitdetective.evidence.collector.RepositoryMetadataCollector;
import com.gitdetective.evidence.collector.StatisticsCollector;
import com.gitdetective.evidence.collector.TimelineCollector;
import com.gitdetective.evidence.collector.TraceCollector;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.validator.EvidenceValidator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentReportSynthesizerTest {

    @Mock private EvidenceEngine evidenceEngine;
    @Mock private IntentDetector intentDetector;
    @Mock private AiProvider aiProvider;

    private IncidentReportSynthesizer synthesizer;
    private EvidenceBundle bundle;

    @BeforeEach
    void setUp() {
        InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();
        EvidenceBundleBuilder builder =
                new EvidenceBundleBuilder(
                        List.of(
                                new RepositoryMetadataCollector(mapper),
                                new InvestigationEvidenceCollector(mapper),
                                new TimelineCollector(mapper),
                                new OwnershipCollector(mapper),
                                new ImpactCollector(mapper),
                                new RelationshipCollector(mapper),
                                new DependencyCollector(mapper),
                                new HotspotCollector(mapper),
                                new PackageHealthCollector(mapper),
                                new StatisticsCollector(mapper),
                                new TraceCollector(mapper),
                                new CommitClusterCollector(mapper)),
                        new EvidenceValidator(),
                        mapper,
                        "1.0.0");
        bundle =
                builder.build(
                        EvidenceTestFixtures.completedDetail(), EvidenceTestFixtures.repository());
        synthesizer =
                new IncidentReportSynthesizer(
                        evidenceEngine,
                        intentDetector,
                        new EvidenceContextBuilder(),
                        new PromptBuilder(),
                        aiProvider,
                        new AssistantEvidenceValidator(new ObjectMapper()),
                        new IncidentReportCompiler());
    }

    @Test
    @DisplayName("discards hallucinated evidence ids and still compiles a factual report")
    void rejectsHallucinatedCitations() {
        when(intentDetector.detect(any())).thenReturn(AssistantIntent.AUTHENTICATION);
        when(aiProvider.complete(any()))
                .thenReturn(
                        """
                        {"answer":"Secret commit deadbeef caused the outage.",
                         "evidenceIds":["99999999-9999-9999-9999-999999999999"],
                         "confidence":99,
                         "referencedFiles":["not-in-repo.java"],
                         "referencedCommits":["deadbeef"],
                         "referencedContributors":["villain@example.com"],
                         "referencedPackages":[]}
                        """);

        IncidentReportResponse report =
                synthesizer.synthesize(
                        EvidenceTestFixtures.INV_ID,
                        "Why did authentication become risky?",
                        bundle,
                        EvidenceTestFixtures.completedDetail());

        assertThat(report.finding()).doesNotContain("deadbeef");
        assertThat(report.finding()).doesNotContain("villain@example.com");
        assertThat(report.timeline()).extracting("commitSha").doesNotContain("deadbeef");
        assertThat(report.keyEvidence())
                .extracting(IncidentReportResponse.EvidenceEntry::evidenceId)
                .doesNotContain(UUID.fromString("99999999-9999-9999-9999-999999999999"));
    }
}
