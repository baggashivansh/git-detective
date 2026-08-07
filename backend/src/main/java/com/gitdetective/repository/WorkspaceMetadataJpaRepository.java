package com.gitdetective.repository;

import com.gitdetective.entity.WorkspaceMetadata;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceMetadataJpaRepository extends JpaRepository<WorkspaceMetadata, UUID> {

    Optional<WorkspaceMetadata> findByWorkspaceKey(String workspaceKey);

    Optional<WorkspaceMetadata> findByPath(String path);

    void deleteByAnalysisSessionId(UUID analysisSessionId);
}
