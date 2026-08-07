package com.gitdetective.git;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

public record GitRepositorySnapshot(
        String name,
        String defaultBranch,
        String remoteUrl,
        long totalCommits,
        long sizeBytes,
        String latestCommitSha,
        Instant earliestCommitAt,
        String detectedPrimaryLanguage,
        List<BranchInfo> branches,
        List<TagInfo> tags,
        List<CommitInfo> commits,
        Path repositoryPath) {

    public record BranchInfo(String name, boolean defaultBranch, String headCommitSha) {}

    public record TagInfo(String name, String commitSha) {}

    public record CommitInfo(
            String sha,
            String authorName,
            String authorEmail,
            Instant authoredAt,
            String message,
            boolean merge,
            List<String> parentShas,
            List<String> branchNames,
            List<String> tagNames,
            int insertions,
            int deletions,
            List<FileChangeInfo> fileChanges) {}

    public record FileChangeInfo(String path, String changeType, int insertions, int deletions) {}
}
