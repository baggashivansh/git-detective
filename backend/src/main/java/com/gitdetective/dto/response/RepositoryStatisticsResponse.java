package com.gitdetective.dto.response;

import java.util.UUID;

public record RepositoryStatisticsResponse(
        UUID repositoryId,
        long totalFiles,
        long totalDirectories,
        long totalLines,
        long totalPackages,
        long totalClasses,
        long totalInterfaces,
        long totalEnums,
        long totalMethods,
        long totalContributors,
        long totalBranches,
        long totalTags,
        long binaryFileCount,
        long ignoredFileCount,
        long totalCommits,
        long sizeBytes) {}
