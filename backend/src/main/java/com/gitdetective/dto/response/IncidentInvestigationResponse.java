package com.gitdetective.dto.response;

import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.incident.IncidentPhase;
import java.time.Instant;
import java.util.UUID;

public record IncidentInvestigationResponse(
        UUID id,
        UUID repositoryId,
        String repositoryName,
        AnalysisStatus repositoryStatus,
        int repositoryProgressPercent,
        String question,
        InvestigationTargetType targetType,
        String targetRef,
        String targetLabel,
        InvestigationStatus status,
        IncidentPhase phase,
        String summary,
        IncidentReportResponse report,
        Instant createdAt,
        Instant completedAt) {}
