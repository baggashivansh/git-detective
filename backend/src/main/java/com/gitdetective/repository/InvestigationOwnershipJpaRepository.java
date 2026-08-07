package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationOwnershipEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationOwnershipJpaRepository
        extends JpaRepository<InvestigationOwnershipEntity, UUID> {

    List<InvestigationOwnershipEntity> findByInvestigationIdOrderByOwnershipPercentageDesc(
            UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
