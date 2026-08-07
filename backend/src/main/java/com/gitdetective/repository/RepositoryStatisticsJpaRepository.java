package com.gitdetective.repository;

import com.gitdetective.entity.RepositoryStatisticsEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositoryStatisticsJpaRepository
        extends JpaRepository<RepositoryStatisticsEntity, UUID> {

    Optional<RepositoryStatisticsEntity> findByRepositoryId(UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
