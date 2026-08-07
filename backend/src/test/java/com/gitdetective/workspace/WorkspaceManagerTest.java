package com.gitdetective.workspace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.gitdetective.config.AnalysisProperties;
import com.gitdetective.entity.AnalysisSession;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.WorkspaceMetadata;
import com.gitdetective.entity.WorkspaceStatus;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.repository.WorkspaceMetadataJpaRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkspaceManagerTest {

    @TempDir Path tempDir;

    @Mock private WorkspaceMetadataJpaRepository workspaceMetadataJpaRepository;

    private WorkspaceManager workspaceManager;

    @BeforeEach
    void setUp() {
        AnalysisProperties properties =
                new AnalysisProperties(tempDir.toString(), 60, 1_000_000, 1000, 1000);
        workspaceManager = new WorkspaceManager(properties, workspaceMetadataJpaRepository);
    }

    @Test
    @DisplayName("creates and cleans workspace safely")
    void createsAndCleansWorkspace() throws Exception {
        AnalysisSession session =
                AnalysisSession.builder()
                        .id(UUID.randomUUID())
                        .repositoryId(UUID.randomUUID())
                        .status(AnalysisStatus.QUEUED)
                        .build();

        when(workspaceMetadataJpaRepository.findByWorkspaceKey("key-1"))
                .thenReturn(Optional.empty());
        when(workspaceMetadataJpaRepository.save(any(WorkspaceMetadata.class)))
                .thenAnswer(
                        invocation -> {
                            WorkspaceMetadata metadata = invocation.getArgument(0);
                            if (metadata.getId() == null) {
                                metadata.setId(UUID.randomUUID());
                            }
                            return metadata;
                        });
        when(workspaceMetadataJpaRepository.findById(any()))
                .thenAnswer(
                        invocation ->
                                Optional.of(
                                        WorkspaceMetadata.builder()
                                                .id(invocation.getArgument(0))
                                                .analysisSessionId(session.getId())
                                                .workspaceKey("key-1")
                                                .path(
                                                        tempDir.resolve(session.getId().toString())
                                                                .toString())
                                                .status(WorkspaceStatus.ACTIVE)
                                                .build()));

        WorkspaceManager.WorkspaceHandle handle =
                workspaceManager.createWorkspace(session, "key-1");

        assertThat(Files.isDirectory(handle.path())).isTrue();
        Files.writeString(handle.path().resolve("marker.txt"), "ok");

        workspaceManager.cleanupWorkspace(handle);
        assertThat(Files.exists(handle.path())).isFalse();
    }

    @Test
    @DisplayName("prevents duplicate active workspaces")
    void preventsDuplicates() {
        AnalysisSession session =
                AnalysisSession.builder()
                        .id(UUID.randomUUID())
                        .repositoryId(UUID.randomUUID())
                        .status(AnalysisStatus.QUEUED)
                        .build();
        when(workspaceMetadataJpaRepository.findByWorkspaceKey("dup"))
                .thenReturn(
                        Optional.of(
                                WorkspaceMetadata.builder()
                                        .id(UUID.randomUUID())
                                        .workspaceKey("dup")
                                        .path("/tmp/x")
                                        .status(WorkspaceStatus.ACTIVE)
                                        .build()));

        assertThatThrownBy(() -> workspaceManager.createWorkspace(session, "dup"))
                .isInstanceOf(RepositoryAnalysisException.class)
                .hasMessageContaining("active workspace");
    }
}
