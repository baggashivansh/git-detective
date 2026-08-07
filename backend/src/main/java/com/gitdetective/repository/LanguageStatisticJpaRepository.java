package com.gitdetective.repository;

import com.gitdetective.entity.LanguageStatisticEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LanguageStatisticJpaRepository
        extends JpaRepository<LanguageStatisticEntity, UUID> {

    List<LanguageStatisticEntity> findByRepositoryIdOrderByPercentageDesc(UUID repositoryId);

    void deleteByRepositoryId(UUID repositoryId);
}
