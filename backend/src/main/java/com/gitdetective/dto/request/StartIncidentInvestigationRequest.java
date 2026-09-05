package com.gitdetective.dto.request;

import com.gitdetective.entity.RepositorySourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record StartIncidentInvestigationRequest(
        UUID repositoryId,
        RepositorySourceType sourceType,
        @Size(max = 2048) String repository,
        @NotBlank @Size(max = 2000) String investigationQuestion) {}
