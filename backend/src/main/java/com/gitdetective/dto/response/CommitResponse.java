package com.gitdetective.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommitResponse(
        UUID id,
        String sha,
        String authorName,
        String authorEmail,
        Instant authoredAt,
        String message,
        boolean merge,
        int insertions,
        int deletions,
        int filesChangedCount,
        List<String> parents,
        List<String> branches,
        List<String> tags) {}
