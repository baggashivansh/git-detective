package com.gitdetective.dto.response;

import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.RepositorySourceType;
import java.time.Instant;
import java.util.UUID;

public record RepositorySummaryResponse(
        UUID id,
        String name,
        RepositorySourceType sourceType,
        String sourceUri,
        String remoteUrl,
        String defaultBranch,
        long totalCommits,
        long sizeBytes,
        String primaryLanguage,
        AnalysisStatus status,
        String statusMessage,
        int progressPercent,
        String errorCode,
        String errorMessage,
        String latestCommitSha,
        Instant createdAt,
        Instant updatedAt,
        Instant analyzedAt) {}
