package com.gitdetective.repository;

import com.gitdetective.entity.FileImportEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileImportJpaRepository extends JpaRepository<FileImportEntity, UUID> {

    List<FileImportEntity> findByFileId(UUID fileId);

    List<FileImportEntity> findByImportNameContainingIgnoreCase(String importName);

    List<FileImportEntity> findByFileIdIn(Collection<UUID> fileIds);

    void deleteByFileId(UUID fileId);
}
