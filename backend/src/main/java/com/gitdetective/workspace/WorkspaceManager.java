package com.gitdetective.workspace;

import com.gitdetective.config.AnalysisProperties;
import com.gitdetective.entity.AnalysisSession;
import com.gitdetective.entity.WorkspaceMetadata;
import com.gitdetective.entity.WorkspaceStatus;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.repository.WorkspaceMetadataJpaRepository;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Creates and safely tears down ephemeral analysis workspaces.
 *
 * <p>Duplicate workspace keys are rejected so the same source cannot be cloned twice concurrently.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkspaceManager {

    private final AnalysisProperties analysisProperties;
    private final WorkspaceMetadataJpaRepository workspaceMetadataJpaRepository;
    private final Map<String, Path> activeWorkspaces = new ConcurrentHashMap<>();

    public WorkspaceHandle createWorkspace(AnalysisSession session, String workspaceKey) {
        if (activeWorkspaces.containsKey(workspaceKey)
                || workspaceMetadataJpaRepository.findByWorkspaceKey(workspaceKey).isPresent()) {
            throw new RepositoryAnalysisException(
                    "DUPLICATE_WORKSPACE",
                    "An active workspace already exists for this repository source");
        }

        Path root = Path.of(analysisProperties.workspaceRoot()).toAbsolutePath().normalize();
        Path workspacePath = root.resolve(session.getId().toString()).normalize();

        if (!workspacePath.startsWith(root)) {
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "INVALID_WORKSPACE_PATH",
                    "Resolved workspace path escapes the configured root");
        }

        try {
            Files.createDirectories(workspacePath);
        } catch (IOException exception) {
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "WORKSPACE_CREATE_FAILED",
                    "Failed to create analysis workspace",
                    exception);
        }

        WorkspaceMetadata metadata =
                WorkspaceMetadata.builder()
                        .analysisSessionId(session.getId())
                        .workspaceKey(workspaceKey)
                        .path(workspacePath.toString())
                        .status(WorkspaceStatus.ACTIVE)
                        .build();
        workspaceMetadataJpaRepository.save(metadata);
        activeWorkspaces.put(workspaceKey, workspacePath);

        log.info(
                "Workspace creation complete sessionId={} path={}", session.getId(), workspacePath);
        return new WorkspaceHandle(metadata.getId(), workspaceKey, workspacePath);
    }

    public void cleanupWorkspace(WorkspaceHandle handle) {
        if (handle == null) {
            return;
        }
        Path path = handle.path().toAbsolutePath().normalize();
        Path root = Path.of(analysisProperties.workspaceRoot()).toAbsolutePath().normalize();
        if (!path.startsWith(root)) {
            log.error("Refusing unsafe workspace cleanup path={}", path);
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "WORKSPACE_CLEANUP_FAILED",
                    "Refusing to delete a path outside the workspace root");
        }

        try {
            if (Files.exists(path)) {
                deleteRecursively(path);
            }
            workspaceMetadataJpaRepository
                    .findById(handle.metadataId())
                    .ifPresent(
                            metadata -> {
                                metadata.setStatus(WorkspaceStatus.CLEANED);
                                metadata.setCleanedAt(Instant.now());
                                workspaceMetadataJpaRepository.save(metadata);
                            });
            activeWorkspaces.remove(handle.workspaceKey());
            log.info("Workspace cleanup complete path={}", path);
        } catch (IOException exception) {
            workspaceMetadataJpaRepository
                    .findById(handle.metadataId())
                    .ifPresent(
                            metadata -> {
                                metadata.setStatus(WorkspaceStatus.FAILED_CLEANUP);
                                workspaceMetadataJpaRepository.save(metadata);
                            });
            log.error("Workspace cleanup failed path={}", path, exception);
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "WORKSPACE_CLEANUP_FAILED",
                    "Failed to clean analysis workspace",
                    exception);
        }
    }

    private void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(
                path,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                            throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    public record WorkspaceHandle(UUID metadataId, String workspaceKey, Path path) {}
}
