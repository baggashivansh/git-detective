package com.gitdetective.analyzer;

import com.gitdetective.dto.request.AnalyzeRepositoryRequest;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.entity.AnalysisSession;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.git.GitEngine;
import com.gitdetective.mapper.RepositoryResponseMapper;
import com.gitdetective.repository.AnalysisSessionJpaRepository;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class RepositoryCommandService {

    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final AnalysisSessionJpaRepository analysisSessionJpaRepository;
    private final RepositoryAnalyzerService repositoryAnalyzerService;
    private final GitEngine gitEngine;
    private final RepositoryResponseMapper repositoryResponseMapper;

    @Transactional
    public RepositorySummaryResponse analyze(AnalyzeRepositoryRequest request) {
        String normalizedSource = normalizeSource(request.sourceType(), request.source());

        Optional<CodeRepository> existing =
                codeRepositoryJpaRepository.findBySourceTypeAndSourceUri(
                        request.sourceType(), normalizedSource);
        if (existing.isPresent()) {
            CodeRepository repository = existing.get();
            if (repository.getStatus() != AnalysisStatus.FAILED
                    && repository.getStatus() != AnalysisStatus.COMPLETED) {
                throw new RepositoryAnalysisException(
                        HttpStatus.CONFLICT,
                        "ANALYSIS_IN_PROGRESS",
                        "An analysis is already running for this repository source");
            }
            if (repository.getStatus() == AnalysisStatus.COMPLETED) {
                return repositoryResponseMapper.toSummary(repository);
            }
            repository.setStatus(AnalysisStatus.QUEUED);
            repository.setStatusMessage("Queued for analysis");
            repository.setProgressPercent(0);
            repository.setErrorCode(null);
            repository.setErrorMessage(null);
            codeRepositoryJpaRepository.save(repository);

            AnalysisSession session =
                    analysisSessionJpaRepository.save(
                            AnalysisSession.builder()
                                    .repositoryId(repository.getId())
                                    .status(AnalysisStatus.QUEUED)
                                    .build());
            queueAnalysis(repository.getId(), session.getId());
            return repositoryResponseMapper.toSummary(repository);
        }

        CodeRepository repository =
                codeRepositoryJpaRepository.save(
                        CodeRepository.builder()
                                .name(extractName(request.sourceType(), normalizedSource))
                                .sourceType(request.sourceType())
                                .sourceUri(normalizedSource)
                                .status(AnalysisStatus.QUEUED)
                                .statusMessage("Queued for analysis")
                                .progressPercent(0)
                                .build());

        AnalysisSession session =
                analysisSessionJpaRepository.save(
                        AnalysisSession.builder()
                                .repositoryId(repository.getId())
                                .status(AnalysisStatus.QUEUED)
                                .build());

        queueAnalysis(repository.getId(), session.getId());
        return repositoryResponseMapper.toSummary(repository);
    }

    private void queueAnalysis(UUID repositoryId, UUID sessionId) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        repositoryAnalyzerService.analyzeAsync(repositoryId, sessionId);
                    }
                });
    }

    private String normalizeSource(RepositorySourceType sourceType, String source) {
        String trimmed = source == null ? "" : source.trim();
        if (trimmed.isBlank()) {
            throw new RepositoryAnalysisException(
                    "INVALID_REPOSITORY", "Repository source is required");
        }
        return switch (sourceType) {
            case GITHUB -> {
                String url = gitEngine.normalizeGitHubUrl(trimmed);
                yield url.endsWith(".git") ? url.substring(0, url.length() - 4) : url;
            }
            case LOCAL -> {
                Path path = Path.of(trimmed).toAbsolutePath().normalize();
                if (!Files.isDirectory(path) || !Files.isDirectory(path.resolve(".git"))) {
                    throw new RepositoryAnalysisException(
                            "INVALID_REPOSITORY",
                            "Local source must be an absolute path to a git repository");
                }
                String lower = trimmed.toLowerCase(Locale.ROOT);
                if (lower.contains("gitlab.com") || lower.contains("bitbucket.org")) {
                    throw new RepositoryAnalysisException(
                            "UNSUPPORTED_REPOSITORY",
                            "GitLab and Bitbucket repositories are not supported");
                }
                yield path.toString();
            }
        };
    }

    private String extractName(RepositorySourceType sourceType, String source) {
        if (sourceType == RepositorySourceType.GITHUB) {
            int idx = source.lastIndexOf('/');
            return idx >= 0 ? source.substring(idx + 1) : source;
        }
        return Path.of(source).getFileName().toString();
    }
}
