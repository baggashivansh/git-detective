package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationJpaRepository extends JpaRepository<InvestigationEntity, UUID> {

    List<InvestigationEntity> findByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);

    List<InvestigationEntity> findAllByOrderByCreatedAtDesc();
}
