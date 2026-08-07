package com.gitdetective.dto.response;

import com.gitdetective.entity.BusFactorLevel;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvestigationSummaryResponse(
        UUID id,
        UUID repositoryId,
        InvestigationTargetType targetType,
        String targetRef,
        String targetLabel,
        InvestigationStatus status,
        String summary,
        Integer busFactorScore,
        BusFactorLevel busFactorLevel,
        BigDecimal blastRadiusScore,
        Instant createdAt,
        Instant completedAt) {}
