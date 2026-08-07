package com.gitdetective.assistant.memory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantConversationJpaRepository
        extends JpaRepository<AssistantConversationEntity, UUID> {

    List<AssistantConversationEntity> findByInvestigationIdOrderByCreatedAtDesc(
            UUID investigationId);
}
