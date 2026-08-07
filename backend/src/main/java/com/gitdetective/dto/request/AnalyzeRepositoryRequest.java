package com.gitdetective.dto.request;

import com.gitdetective.entity.RepositorySourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalyzeRepositoryRequest(
        @NotNull RepositorySourceType sourceType, @NotBlank String source) {}
