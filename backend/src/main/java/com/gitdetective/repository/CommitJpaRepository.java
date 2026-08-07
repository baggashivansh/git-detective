package com.gitdetective.repository;

import com.gitdetective.entity.CommitEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommitJpaRepository extends JpaRepository<CommitEntity, UUID> {

    Optional<CommitEntity> findByRepositoryIdAndSha(UUID repositoryId, String sha);

    Optional<CommitEntity> findByIdAndRepositoryId(UUID id, UUID repositoryId);

    List<CommitEntity> findByRepositoryIdAndAuthorEmailIgnoreCaseOrderByAuthoredAtDesc(
            UUID repositoryId, String authorEmail);

    Page<CommitEntity> findByRepositoryIdOrderByAuthoredAtDesc(
            UUID repositoryId, Pageable pageable);

    List<CommitEntity> findByRepositoryIdAndShaContainingIgnoreCase(UUID repositoryId, String sha);

    List<CommitEntity> findByRepositoryIdAndMessageContainingIgnoreCase(
            UUID repositoryId, String message);

    long countByRepositoryId(UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
