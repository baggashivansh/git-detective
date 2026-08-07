package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationHotspotEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationHotspotJpaRepository
        extends JpaRepository<InvestigationHotspotEntity, UUID> {

    List<InvestigationHotspotEntity> findByInvestigationIdOrderByRankPositionAsc(
            UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
