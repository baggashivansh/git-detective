package com.gitdetective.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ContributorResponse(
        UUID id,
        String name,
        String email,
        long commitCount,
        long filesModified,
        long linesAdded,
        long linesDeleted,
        Instant lastContributionAt,
        BigDecimal contributionPercentage) {}
