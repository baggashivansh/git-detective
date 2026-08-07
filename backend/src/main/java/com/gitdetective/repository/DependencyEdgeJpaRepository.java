package com.gitdetective.repository;

import com.gitdetective.entity.DependencyEdgeEntity;
import com.gitdetective.entity.DependencyRelationship;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DependencyEdgeJpaRepository extends JpaRepository<DependencyEdgeEntity, UUID> {

    List<DependencyEdgeEntity> findByRepositoryId(UUID repositoryId);

    List<DependencyEdgeEntity> findByRepositoryIdAndSourceNodeId(
            UUID repositoryId, UUID sourceNodeId);

    List<DependencyEdgeEntity> findByRepositoryIdAndTargetNodeId(
            UUID repositoryId, UUID targetNodeId);

    List<DependencyEdgeEntity> findByRepositoryIdAndRelationship(
            UUID repositoryId, DependencyRelationship relationship);

    Optional<DependencyEdgeEntity> findByRepositoryIdAndSourceNodeIdAndTargetNodeIdAndRelationship(
            UUID repositoryId,
            UUID sourceNodeId,
            UUID targetNodeId,
            DependencyRelationship relationship);

    void deleteByRepositoryId(UUID repositoryId);
}
