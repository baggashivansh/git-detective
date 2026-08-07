package com.gitdetective.repository;

import com.gitdetective.entity.ContributorEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContributorJpaRepository extends JpaRepository<ContributorEntity, UUID> {

    List<ContributorEntity> findByRepositoryIdOrderByCommitCountDesc(UUID repositoryId);

    Optional<ContributorEntity> findByRepositoryIdAndEmailIgnoreCase(
            UUID repositoryId, String email);

    Optional<ContributorEntity> findByIdAndRepositoryId(UUID id, UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
