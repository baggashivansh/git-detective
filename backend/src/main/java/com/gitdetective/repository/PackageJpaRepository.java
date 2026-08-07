package com.gitdetective.repository;

import com.gitdetective.entity.PackageEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageJpaRepository extends JpaRepository<PackageEntity, UUID> {

    List<PackageEntity> findByRepositoryIdOrderByNameAsc(UUID repositoryId);

    Optional<PackageEntity> findByRepositoryIdAndName(UUID repositoryId, String name);

    Optional<PackageEntity> findByIdAndRepositoryId(UUID id, UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
