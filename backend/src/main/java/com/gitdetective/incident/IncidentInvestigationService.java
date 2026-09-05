package com.gitdetective.incident;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.analyzer.RepositoryCommandService;
import com.gitdetective.assistant.prompt.PromptBuilder;
import com.gitdetective.dto.request.AnalyzeRepositoryRequest;
import com.gitdetective.dto.request.CreateInvestigationRequest;
import com.gitdetective.dto.request.StartIncidentInvestigationRequest;
import com.gitdetective.dto.response.IncidentInvestigationResponse;
import com.gitdetective.dto.response.IncidentReportResponse;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.InvestigationEntity;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.exception.ResourceNotFoundException;
import com.gitdetective.git.GitEngine;
import com.gitdetective.investigation.InvestigationService;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import com.gitdetective.repository.InvestigationJpaRepository;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentInvestigationService {

    private final RepositoryCommandService repositoryCommandService;
    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final InvestigationJpaRepository investigationJpaRepository;
    private final InvestigationService investigationService;
    private final IncidentScopeResolver scopeResolver;
    private final IncidentReportSynthesizer reportSynthesizer;
    private final PromptBuilder promptBuilder;
    private final GitEngine gitEngine;
    private final ObjectMapper objectMapper;

    @Transactional
    public IncidentInvestigationResponse start(StartIncidentInvestigationRequest request) {
        String question = normalizeQuestion(request.investigationQuestion());
        CodeRepository repository = resolveOrAnalyzeRepository(request);
        InvestigationEntity existing =
                investigationJpaRepository
                        .findFirstByRepositoryIdAndQuestionAndStatusOrderByCreatedAtDesc(
                                repository.getId(), question, InvestigationStatus.COMPLETED)
                        .orElse(null);
        if (existing != null) {
            return existing.getIncidentReport() != null
                    ? toResponse(existing, repository, readReport(existing))
                    : advance(existing.getId());
        }

        InvestigationEntity investigation =
                investigationJpaRepository
                        .findFirstByRepositoryIdAndQuestionAndStatusOrderByCreatedAtDesc(
                                repository.getId(), question, InvestigationStatus.QUEUED)
                        .orElseGet(
                                () ->
                                        investigationJpaRepository
                                                .findFirstByRepositoryIdAndQuestionAndStatusOrderByCreatedAtDesc(
                                                        repository.getId(),
                                                        question,
                                                        InvestigationStatus.RUNNING)
                                                .orElse(null));
        if (investigation == null) {
            investigation =
                    investigationJpaRepository.save(
                            InvestigationEntity.builder()
                                    .repositoryId(repository.getId())
                                    .targetType(InvestigationTargetType.BRANCH)
                                    .targetRef("pending")
                                    .targetLabel("Pending repository analysis")
                                    .status(InvestigationStatus.QUEUED)
                                    .question(question)
                                    .build());
        }
        return advance(investigation.getId());
    }

    @Transactional
    public IncidentInvestigationResponse advance(UUID id) {
        InvestigationEntity investigation = require(id);
        CodeRepository repository = requireRepository(investigation.getRepositoryId());
        String question = investigation.getQuestion();
        if (question == null || question.isBlank()) {
            throw new RepositoryAnalysisException(
                    "NOT_AN_INCIDENT", "Investigation is not a question-driven incident");
        }

        if (repository.getStatus() == AnalysisStatus.FAILED) {
            investigation.setStatus(InvestigationStatus.FAILED);
            investigation.setSummary(
                    repository.getErrorMessage() == null
                            ? "Repository analysis failed"
                            : repository.getErrorMessage());
            investigationJpaRepository.save(investigation);
            return toResponse(investigation, repository, null);
        }

        if (repository.getStatus() != AnalysisStatus.COMPLETED) {
            return toResponse(investigation, repository, readReport(investigation));
        }

        if (investigation.getStatus() == InvestigationStatus.COMPLETED
                && investigation.getIncidentReport() != null) {
            return toResponse(investigation, repository, readReport(investigation));
        }

        if (investigation.getStatus() == InvestigationStatus.FAILED
                || investigation.getStatus() == InvestigationStatus.RUNNING) {
            return toResponse(investigation, repository, readReport(investigation));
        }

        if (investigation.getStatus() != InvestigationStatus.COMPLETED) {
            IncidentScope scope = scopeResolver.resolve(repository.getId(), question);
            try {
                investigationService.runQueued(
                        investigation.getId(),
                        new CreateInvestigationRequest(
                                repository.getId(), scope.targetType(), scope.targetRef()));
            } catch (RuntimeException ex) {
                InvestigationEntity failed = require(id);
                failed.setStatus(InvestigationStatus.FAILED);
                failed.setSummary(ex.getMessage());
                investigationJpaRepository.save(failed);
                throw ex;
            }
            investigation = require(id);
        }

        if (investigation.getStatus() != InvestigationStatus.COMPLETED) {
            return toResponse(investigation, repository, readReport(investigation));
        }

        InvestigationDetailResponse detail = investigationService.get(investigation.getId());
        IncidentReportResponse report =
                reportSynthesizer.synthesize(investigation.getId(), question, null, detail);
        investigation.setIncidentReport(writeReport(report));
        if (investigation.getStatus() != InvestigationStatus.COMPLETED) {
            investigation.setStatus(InvestigationStatus.COMPLETED);
        }
        investigationJpaRepository.save(investigation);
        return toResponse(investigation, repository, report);
    }

    @Transactional(readOnly = true)
    public IncidentInvestigationResponse get(UUID id) {
        InvestigationEntity investigation = require(id);
        if (investigation.getQuestion() == null) {
            throw new RepositoryAnalysisException(
                    "NOT_AN_INCIDENT", "Investigation is not a question-driven incident");
        }
        CodeRepository repository = requireRepository(investigation.getRepositoryId());
        return toResponse(investigation, repository, readReport(investigation));
    }

    @Transactional(readOnly = true)
    public List<IncidentInvestigationResponse> list() {
        return investigationJpaRepository.findByQuestionIsNotNullOrderByCreatedAtDesc().stream()
                .map(
                        entity ->
                                toResponse(
                                        entity,
                                        requireRepository(entity.getRepositoryId()),
                                        readReport(entity)))
                .toList();
    }

    String normalizeQuestion(String raw) {
        return promptBuilder.sanitize(raw).replaceAll("\\s+", " ").strip();
    }

    private CodeRepository resolveOrAnalyzeRepository(StartIncidentInvestigationRequest request) {
        if (request.repositoryId() != null) {
            return requireRepository(request.repositoryId());
        }
        if (request.repository() == null || request.repository().isBlank()) {
            throw new RepositoryAnalysisException(
                    "INVALID_REPOSITORY", "Provide repositoryId or a repository source");
        }
        RepositorySourceType sourceType =
                request.sourceType() == null ? RepositorySourceType.GITHUB : request.sourceType();
        try {
            RepositorySummaryResponse summary =
                    repositoryCommandService.analyze(
                            new AnalyzeRepositoryRequest(sourceType, request.repository()));
            return requireRepository(summary.id());
        } catch (RepositoryAnalysisException ex) {
            if (!"ANALYSIS_IN_PROGRESS".equals(ex.getErrorCode())) {
                throw ex;
            }
            return codeRepositoryJpaRepository
                    .findBySourceTypeAndSourceUri(
                            sourceType, normalizeSource(sourceType, request.repository()))
                    .orElseThrow(() -> ex);
        }
    }

    private String normalizeSource(RepositorySourceType sourceType, String source) {
        String trimmed = source == null ? "" : source.trim();
        if (sourceType == RepositorySourceType.GITHUB) {
            String url = gitEngine.normalizeGitHubUrl(trimmed);
            return url.endsWith(".git") ? url.substring(0, url.length() - 4) : url;
        }
        return Path.of(trimmed).toAbsolutePath().normalize().toString();
    }

    private InvestigationEntity require(UUID id) {
        return investigationJpaRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Incident investigation not found: " + id));
    }

    private CodeRepository requireRepository(UUID id) {
        return codeRepositoryJpaRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found: " + id));
    }

    private IncidentInvestigationResponse toResponse(
            InvestigationEntity investigation,
            CodeRepository repository,
            IncidentReportResponse report) {
        return new IncidentInvestigationResponse(
                investigation.getId(),
                repository.getId(),
                repository.getName(),
                repository.getStatus(),
                repository.getProgressPercent(),
                investigation.getQuestion(),
                investigation.getTargetType(),
                investigation.getTargetRef(),
                investigation.getTargetLabel(),
                investigation.getStatus(),
                phase(investigation, repository, report),
                investigation.getSummary(),
                report,
                investigation.getCreatedAt(),
                investigation.getCompletedAt());
    }

    static IncidentPhase phase(
            InvestigationEntity investigation,
            CodeRepository repository,
            IncidentReportResponse report) {
        if (investigation.getStatus() == InvestigationStatus.FAILED
                || repository.getStatus() == AnalysisStatus.FAILED) {
            return IncidentPhase.FAILED;
        }
        if (investigation.getStatus() == InvestigationStatus.COMPLETED && report != null) {
            return IncidentPhase.COMPLETED;
        }
        if (repository.getStatus() != AnalysisStatus.COMPLETED) {
            return IncidentPhase.ANALYZING;
        }
        if (investigation.getStatus() == InvestigationStatus.COMPLETED) {
            return IncidentPhase.VALIDATING;
        }
        return IncidentPhase.INVESTIGATING;
    }

    private IncidentReportResponse readReport(InvestigationEntity investigation) {
        String json = investigation.getIncidentReport();
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, IncidentReportResponse.class);
        } catch (Exception ex) {
            log.warn("Failed to parse stored incident report id={}", investigation.getId());
            return null;
        }
    }

    private String writeReport(IncidentReportResponse report) {
        try {
            return objectMapper.writeValueAsString(report);
        } catch (Exception ex) {
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "REPORT_SERIALIZATION_FAILED",
                    "Failed to persist incident report",
                    ex);
        }
    }
}
