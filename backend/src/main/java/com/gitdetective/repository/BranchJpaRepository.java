package com.gitdetective.repository;

import com.gitdetective.entity.BranchEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchJpaRepository extends JpaRepository<BranchEntity, UUID> {

    List<BranchEntity> findByRepositoryId(UUID repositoryId);

    Optional<BranchEntity> findByRepositoryIdAndName(UUID repositoryId, String name);

    Optional<BranchEntity> findByIdAndRepositoryId(UUID id, UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
