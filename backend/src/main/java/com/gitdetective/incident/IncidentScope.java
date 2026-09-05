package com.gitdetective.incident;

import com.gitdetective.entity.InvestigationTargetType;

public record IncidentScope(
        InvestigationTargetType targetType, String targetRef, String rationale) {}
