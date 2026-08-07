package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationCommitClusterEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationCommitClusterJpaRepository
        extends JpaRepository<InvestigationCommitClusterEntity, UUID> {

    List<InvestigationCommitClusterEntity> findByInvestigationId(UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
