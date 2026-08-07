package com.gitdetective.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateAssistantConversationRequest(@NotNull UUID investigationId) {}
