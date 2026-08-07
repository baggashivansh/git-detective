package com.gitdetective.dto.request;

import com.gitdetective.entity.InvestigationTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateInvestigationRequest(
        @NotNull UUID repositoryId,
        @NotNull InvestigationTargetType targetType,
        @NotBlank String targetRef) {}
