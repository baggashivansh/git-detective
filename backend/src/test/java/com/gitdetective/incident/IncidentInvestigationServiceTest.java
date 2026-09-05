package com.gitdetective.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gitdetective.analyzer.RepositoryCommandService;
import com.gitdetective.assistant.prompt.PromptBuilder;
import com.gitdetective.dto.request.StartIncidentInvestigationRequest;
import com.gitdetective.dto.response.IncidentInvestigationResponse;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.InvestigationEntity;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.git.GitEngine;
import com.gitdetective.investigation.InvestigationService;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import com.gitdetective.repository.InvestigationJpaRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentInvestigationServiceTest {

    @Mock private RepositoryCommandService repositoryCommandService;
    @Mock private CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    @Mock private InvestigationJpaRepository investigationJpaRepository;
    @Mock private InvestigationService investigationService;
    @Mock private IncidentScopeResolver scopeResolver;
    @Mock private IncidentReportSynthesizer reportSynthesizer;
    @Mock private GitEngine gitEngine;

    private IncidentInvestigationService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        service =
                new IncidentInvestigationService(
                        repositoryCommandService,
                        codeRepositoryJpaRepository,
                        investigationJpaRepository,
                        investigationService,
                        scopeResolver,
                        reportSynthesizer,
                        new PromptBuilder(),
                        gitEngine,
                        objectMapper);
    }

    @Test
    @DisplayName("reuses a completed incident for the same repository and question")
    void reusesCompletedIncident() throws Exception {
        UUID repoId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        CodeRepository repository = completedRepo(repoId);
        InvestigationEntity entity =
                InvestigationEntity.builder()
                        .id(incidentId)
                        .repositoryId(repoId)
                        .targetType(InvestigationTargetType.BRANCH)
                        .targetRef("main")
                        .targetLabel("main")
                        .status(InvestigationStatus.COMPLETED)
                        .question("Why did authentication become risky after recent changes?")
                        .incidentReport(
                                objectMapper.writeValueAsString(
                                        new IncidentReportCompiler()
                                                .compile(
                                                        "Why did authentication become risky after recent changes?",
                                                        EvidenceTestFixtures.completedDetail(),
                                                        null,
                                                        null)))
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .completedAt(Instant.now())
                        .build();

        when(codeRepositoryJpaRepository.findById(repoId)).thenReturn(Optional.of(repository));
        when(investigationJpaRepository
                        .findFirstByRepositoryIdAndQuestionAndStatusOrderByCreatedAtDesc(
                                any(), any(), any()))
                .thenReturn(Optional.of(entity));

        IncidentInvestigationResponse response =
                service.start(
                        new StartIncidentInvestigationRequest(
                                repoId,
                                null,
                                null,
                                "Why did authentication become risky after recent changes?"));

        assertThat(response.id()).isEqualTo(incidentId);
        assertThat(response.phase()).isEqualTo(IncidentPhase.COMPLETED);
        assertThat(response.report()).isNotNull();
        verify(investigationService, never()).runQueued(any(), any());
    }

    @Test
    @DisplayName("waits for repository analysis instead of rescanning a completed source")
    void returnsAnalyzingWhenRepositoryNotReady() {
        UUID repoId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        CodeRepository repository =
                CodeRepository.builder()
                        .id(repoId)
                        .name("demo")
                        .sourceType(RepositorySourceType.GITHUB)
                        .sourceUri("https://github.com/acme/demo")
                        .status(AnalysisStatus.SCANNING)
                        .progressPercent(40)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
        when(repositoryCommandService.analyze(any()))
                .thenReturn(
                        new RepositorySummaryResponse(
                                repoId,
                                "demo",
                                RepositorySourceType.GITHUB,
                                "https://github.com/acme/demo",
                                null,
                                "main",
                                0,
                                0,
                                null,
                                AnalysisStatus.SCANNING,
                                "Scanning",
                                40,
                                null,
                                null,
                                null,
                                Instant.now(),
                                Instant.now(),
                                null));
        when(codeRepositoryJpaRepository.findById(repoId)).thenReturn(Optional.of(repository));
        when(investigationJpaRepository
                        .findFirstByRepositoryIdAndQuestionAndStatusOrderByCreatedAtDesc(
                                any(), any(), any()))
                .thenReturn(Optional.empty());
        when(investigationJpaRepository.save(any()))
                .thenAnswer(
                        invocation -> {
                            InvestigationEntity saved = invocation.getArgument(0);
                            if (saved.getId() == null) {
                                saved.setId(incidentId);
                            }
                            return saved;
                        });
        when(investigationJpaRepository.findById(incidentId))
                .thenAnswer(
                        invocation ->
                                Optional.of(
                                        InvestigationEntity.builder()
                                                .id(incidentId)
                                                .repositoryId(repoId)
                                                .targetType(InvestigationTargetType.BRANCH)
                                                .targetRef("pending")
                                                .targetLabel("Pending repository analysis")
                                                .status(InvestigationStatus.QUEUED)
                                                .question(
                                                        "Why did authentication become risky after recent changes?")
                                                .createdAt(Instant.now())
                                                .updatedAt(Instant.now())
                                                .build()));

        IncidentInvestigationResponse response =
                service.start(
                        new StartIncidentInvestigationRequest(
                                null,
                                RepositorySourceType.GITHUB,
                                "https://github.com/acme/demo",
                                "Why did authentication become risky after recent changes?"));

        assertThat(response.phase()).isEqualTo(IncidentPhase.ANALYZING);
        assertThat(response.repositoryProgressPercent()).isEqualTo(40);
        verify(investigationService, never()).runQueued(any(), any());
    }

    @Test
    @DisplayName("normalizes questions before reuse lookup")
    void normalizesQuestion() {
        assertThat(service.normalizeQuestion("  Why   did authentication   become risky?  "))
                .isEqualTo("Why did authentication become risky?");
    }

    @Test
    @DisplayName("does not synthesize while another continue is still running engines")
    void doesNotSynthesizeWhenStillRunning() {
        UUID repoId = UUID.randomUUID();
        UUID incidentId = UUID.randomUUID();
        CodeRepository repository = completedRepo(repoId);
        InvestigationEntity running =
                InvestigationEntity.builder()
                        .id(incidentId)
                        .repositoryId(repoId)
                        .targetType(InvestigationTargetType.BRANCH)
                        .targetRef("main")
                        .targetLabel("main")
                        .status(InvestigationStatus.RUNNING)
                        .question("Who owns Demo?")
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

        when(investigationJpaRepository.findById(incidentId)).thenReturn(Optional.of(running));
        when(codeRepositoryJpaRepository.findById(repoId)).thenReturn(Optional.of(repository));

        IncidentInvestigationResponse response = service.advance(incidentId);

        assertThat(response.phase()).isEqualTo(IncidentPhase.INVESTIGATING);
        assertThat(response.report()).isNull();
        verify(reportSynthesizer, never()).synthesize(any(), any(), any(), any());
        verify(investigationService, never()).runQueued(eq(incidentId), any());
    }

    @Test
    @DisplayName("phase is FAILED when analysis failed")
    void failedPhase() {
        InvestigationEntity investigation =
                InvestigationEntity.builder().status(InvestigationStatus.FAILED).build();
        CodeRepository repository = CodeRepository.builder().status(AnalysisStatus.FAILED).build();
        assertThat(IncidentInvestigationService.phase(investigation, repository, null))
                .isEqualTo(IncidentPhase.FAILED);
    }

    private static CodeRepository completedRepo(UUID repoId) {
        return CodeRepository.builder()
                .id(repoId)
                .name("demo")
                .sourceType(RepositorySourceType.GITHUB)
                .sourceUri("https://github.com/acme/demo")
                .status(AnalysisStatus.COMPLETED)
                .progressPercent(100)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
}
