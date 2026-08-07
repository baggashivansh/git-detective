package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationImpactItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationImpactItemJpaRepository
        extends JpaRepository<InvestigationImpactItemEntity, UUID> {

    List<InvestigationImpactItemEntity> findByInvestigationIdOrderByDependencyDepthAsc(
            UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
