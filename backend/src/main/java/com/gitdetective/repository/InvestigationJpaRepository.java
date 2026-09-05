package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationEntity;
import com.gitdetective.entity.InvestigationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationJpaRepository extends JpaRepository<InvestigationEntity, UUID> {

    List<InvestigationEntity> findByRepositoryIdOrderByCreatedAtDesc(UUID repositoryId);

    List<InvestigationEntity> findAllByOrderByCreatedAtDesc();

    List<InvestigationEntity> findByQuestionIsNotNullOrderByCreatedAtDesc();

    Optional<InvestigationEntity> findFirstByRepositoryIdAndQuestionAndStatusOrderByCreatedAtDesc(
            UUID repositoryId, String question, InvestigationStatus status);
}
