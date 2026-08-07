package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationPackageHealthEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationPackageHealthJpaRepository
        extends JpaRepository<InvestigationPackageHealthEntity, UUID> {

    List<InvestigationPackageHealthEntity> findByInvestigationIdOrderByRiskLevelDesc(
            UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
