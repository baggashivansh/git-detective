package com.gitdetective.repository;

import com.gitdetective.entity.InvestigationTimelineEventEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationTimelineEventJpaRepository
        extends JpaRepository<InvestigationTimelineEventEntity, UUID> {

    List<InvestigationTimelineEventEntity> findByInvestigationIdOrderByOccurredAtAscSortOrderAsc(
            UUID investigationId);

    void deleteByInvestigationId(UUID investigationId);
}
