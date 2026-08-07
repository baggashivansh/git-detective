package com.gitdetective.repository;

import com.gitdetective.entity.CodeTypeEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodeTypeJpaRepository extends JpaRepository<CodeTypeEntity, UUID> {

    List<CodeTypeEntity> findByRepositoryIdOrderByFullyQualifiedNameAsc(UUID repositoryId);

    List<CodeTypeEntity> findByRepositoryIdAndNameContainingIgnoreCase(
            UUID repositoryId, String name);

    Optional<CodeTypeEntity> findByRepositoryIdAndFullyQualifiedName(
            UUID repositoryId, String fullyQualifiedName);

    Optional<CodeTypeEntity> findByIdAndRepositoryId(UUID id, UUID repositoryId);

    List<CodeTypeEntity> findByRepositoryIdAndFileId(UUID repositoryId, UUID fileId);

    void deleteByRepositoryId(UUID repositoryId);
}
