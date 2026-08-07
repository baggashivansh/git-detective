package com.gitdetective.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskAssistantRequest(@NotBlank @Size(max = 2000) String question) {}
