package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationRelationshipEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationRelationshipJpaRepository
        extends JpaRepository<InvestigationRelationshipEntity, UUID> {

    List<InvestigationRelationshipEntity> findByInvestigationId(UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
