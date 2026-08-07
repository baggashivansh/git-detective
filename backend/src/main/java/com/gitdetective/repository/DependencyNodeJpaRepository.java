package com.gitdetective.repository;

import com.gitdetective.entity.DependencyNodeEntity;
import com.gitdetective.entity.DependencyNodeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependencyNodeJpaRepository extends JpaRepository<DependencyNodeEntity, UUID> {

    List<DependencyNodeEntity> findByRepositoryId(UUID repositoryId);

    List<DependencyNodeEntity> findByRepositoryIdAndNodeType(
            UUID repositoryId, DependencyNodeType nodeType);

    List<DependencyNodeEntity> findByRepositoryIdAndLabelContainingIgnoreCase(
            UUID repositoryId, String label);

    Optional<DependencyNodeEntity> findByRepositoryIdAndNodeKey(UUID repositoryId, String nodeKey);

    void deleteByRepositoryId(UUID repositoryId);
}
