package com.gitdetective.repository;

import com.gitdetective.entity.FileExportEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileExportJpaRepository extends JpaRepository<FileExportEntity, UUID> {

    List<FileExportEntity> findByFileId(UUID fileId);

    void deleteByFileId(UUID fileId);
}
