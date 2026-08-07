package com.gitdetective.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gitdetective.analysis")
public record AnalysisProperties(
        String workspaceRoot,
        int cloneTimeoutSeconds,
        long maxRepositorySizeBytes,
        int maxFiles,
        int maxCommits) {}
