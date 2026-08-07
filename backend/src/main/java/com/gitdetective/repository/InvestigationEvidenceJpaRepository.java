package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationEvidenceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationEvidenceJpaRepository
        extends JpaRepository<InvestigationEvidenceEntity, UUID> {

    List<InvestigationEvidenceEntity> findByInvestigationIdOrderBySortOrderAsc(
            UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
