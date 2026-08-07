package com.gitdetective.dto.response;

import com.gitdetective.assistant.formatter.AssistantResponseFormatter.AssistantAnswer;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssistantConversationResponse(
        UUID id,
        UUID repositoryId,
        UUID investigationId,
        String title,
        Instant createdAt,
        Instant updatedAt,
        List<AssistantMessageResponse> messages,
        List<String> suggestedQuestions) {

    public record AssistantMessageResponse(
            UUID id,
            String role,
            String content,
            String intent,
            Integer confidence,
            Instant createdAt,
            AssistantAnswer answer) {}
}
