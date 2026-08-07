package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationTraceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationTraceJpaRepository
        extends JpaRepository<InvestigationTraceEntity, UUID> {

    List<InvestigationTraceEntity> findByInvestigationIdOrderByStepOrderAsc(UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
