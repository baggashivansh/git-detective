package com.gitdetective.analyzer;

import com.gitdetective.entity.AnalysisSession;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.git.GitEngine;
import com.gitdetective.git.GitRepositorySnapshot;
import com.gitdetective.indexer.FileSystemIndexer;
import com.gitdetective.indexer.IndexedFile;
import com.gitdetective.repository.AnalysisSessionJpaRepository;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import com.gitdetective.workspace.WorkspaceManager;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryAnalyzerService {

    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final AnalysisSessionJpaRepository analysisSessionJpaRepository;
    private final WorkspaceManager workspaceManager;
    private final GitEngine gitEngine;
    private final FileSystemIndexer fileSystemIndexer;
    private final RepositoryAnalysisPersistenceService persistenceService;

    @Async("repositoryAnalysisExecutor")
    public void analyzeAsync(UUID repositoryId, UUID sessionId) {
        CodeRepository repository = codeRepositoryJpaRepository.findById(repositoryId).orElse(null);
        AnalysisSession session = analysisSessionJpaRepository.findById(sessionId).orElse(null);
        if (repository == null || session == null) {
            return;
        }

        WorkspaceManager.WorkspaceHandle workspace = null;
        Instant started = Instant.now();
        try {
            String workspaceKey = repository.getSourceType() + "::" + repository.getSourceUri();
            workspace = workspaceManager.createWorkspace(session, workspaceKey);
            session.setWorkspacePath(workspace.path().toString());
            analysisSessionJpaRepository.save(session);

            update(repositoryId, sessionId, AnalysisStatus.CLONING, "Cloning repository", 10);
            Path repoPath =
                    gitEngine.materializeRepository(
                            repository.getSourceType(),
                            repository.getSourceUri(),
                            workspace.path());

            update(repositoryId, sessionId, AnalysisStatus.SCANNING, "Collecting git metadata", 30);
            GitRepositorySnapshot snapshot = gitEngine.collectSnapshot(repoPath);
            persistenceService.persistGitSnapshot(repositoryId, snapshot);

            update(repositoryId, sessionId, AnalysisStatus.PARSING, "Scanning filesystem", 55);
            List<IndexedFile> indexedFiles = fileSystemIndexer.indexRepository(repoPath);

            update(repositoryId, sessionId, AnalysisStatus.INDEXING, "Indexing metadata", 75);
            persistenceService.persistIndexedFiles(repositoryId, indexedFiles);

            CodeRepository completed =
                    codeRepositoryJpaRepository.findById(repositoryId).orElseThrow();
            completed.setStatus(AnalysisStatus.COMPLETED);
            completed.setStatusMessage("Analysis completed");
            completed.setProgressPercent(100);
            completed.setAnalyzedAt(Instant.now());
            completed.setErrorCode(null);
            completed.setErrorMessage(null);
            codeRepositoryJpaRepository.save(completed);

            session.setStatus(AnalysisStatus.COMPLETED);
            session.setFinishedAt(Instant.now());
            session.setDurationMs(Duration.between(started, session.getFinishedAt()).toMillis());
            analysisSessionJpaRepository.save(session);

            log.info(
                    "Repository analysis completed repositoryId={} durationMs={}",
                    repositoryId,
                    session.getDurationMs());
        } catch (RepositoryAnalysisException exception) {
            fail(
                    repositoryId,
                    sessionId,
                    started,
                    exception.getErrorCode(),
                    exception.getMessage());
            log.error(
                    "Repository analysis failed repositoryId={} code={}",
                    repositoryId,
                    exception.getErrorCode(),
                    exception);
        } catch (Exception exception) {
            fail(
                    repositoryId,
                    sessionId,
                    started,
                    "ANALYSIS_FAILED",
                    "Unexpected analysis failure");
            log.error("Repository analysis failed repositoryId={}", repositoryId, exception);
        } finally {
            if (workspace != null) {
                try {
                    workspaceManager.cleanupWorkspace(workspace);
                } catch (Exception cleanupException) {
                    log.error(
                            "Workspace cleanup failure repositoryId={}",
                            repositoryId,
                            cleanupException);
                }
            }
        }
    }

    private void update(
            UUID repositoryId,
            UUID sessionId,
            AnalysisStatus status,
            String message,
            int progress) {
        persistenceService.updateStatus(repositoryId, status, message, progress);
        analysisSessionJpaRepository
                .findById(sessionId)
                .ifPresent(
                        session -> {
                            session.setStatus(status);
                            analysisSessionJpaRepository.save(session);
                        });
        log.info(
                "Analysis progress repositoryId={} status={} progress={}",
                repositoryId,
                status,
                progress);
    }

    private void fail(
            UUID repositoryId, UUID sessionId, Instant started, String errorCode, String message) {
        codeRepositoryJpaRepository
                .findById(repositoryId)
                .ifPresent(
                        repository -> {
                            repository.setStatus(AnalysisStatus.FAILED);
                            repository.setStatusMessage(message);
                            repository.setProgressPercent(100);
                            repository.setErrorCode(errorCode);
                            repository.setErrorMessage(message);
                            codeRepositoryJpaRepository.save(repository);
                        });
        analysisSessionJpaRepository
                .findById(sessionId)
                .ifPresent(
                        session -> {
                            session.setStatus(AnalysisStatus.FAILED);
                            session.setErrorCode(errorCode);
                            session.setErrorMessage(message);
                            session.setFinishedAt(Instant.now());
                            session.setDurationMs(
                                    Duration.between(started, session.getFinishedAt()).toMillis());
                            analysisSessionJpaRepository.save(session);
                        });
    }

    public String workspaceKey(RepositorySourceType sourceType, String sourceUri) {
        return sourceType + "::" + sourceUri;
    }
}
