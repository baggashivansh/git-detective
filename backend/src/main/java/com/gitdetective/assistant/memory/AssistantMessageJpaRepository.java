package com.gitdetective.assistant.memory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssistantMessageJpaRepository extends JpaRepository<AssistantMessageEntity, UUID> {

    List<AssistantMessageEntity> findByConversationIdOrderBySortOrderAsc(UUID conversationId);

    long countByConversationId(UUID conversationId);
}
