package com.gitdetective.repository;

import com.gitdetective.entity.FileEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileJpaRepository extends JpaRepository<FileEntity, UUID> {

    Optional<FileEntity> findByRepositoryIdAndPath(UUID repositoryId, String path);

    Optional<FileEntity> findByIdAndRepositoryId(UUID id, UUID repositoryId);

    List<FileEntity> findByRepositoryIdAndDirectoryFalse(UUID repositoryId);

    List<FileEntity> findByRepositoryIdOrderByPathAsc(UUID repositoryId);

    List<FileEntity> findByRepositoryIdAndPathContainingIgnoreCase(UUID repositoryId, String path);

    List<FileEntity> findByRepositoryIdAndNameContainingIgnoreCase(UUID repositoryId, String name);

    void deleteByRepositoryId(UUID repositoryId);
}
