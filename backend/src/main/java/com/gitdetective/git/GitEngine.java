package com.gitdetective.git;

import com.gitdetective.config.AnalysisProperties;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.parser.LanguageDetector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitEngine {

    private final AnalysisProperties analysisProperties;
    private final LanguageDetector languageDetector;

    public Path materializeRepository(
            RepositorySourceType sourceType, String sourceUri, Path workspacePath) {
        return switch (sourceType) {
            case GITHUB -> clonePublicGitHubRepository(sourceUri, workspacePath);
            case LOCAL -> copyValidateLocalRepository(sourceUri, workspacePath);
        };
    }

    public GitRepositorySnapshot collectSnapshot(Path repositoryPath) {
        long started = System.currentTimeMillis();
        log.info("Git engine collection start path={}", repositoryPath);
        try (Repository repository = openRepository(repositoryPath);
                Git git = new Git(repository)) {
            validateRepository(repository);

            String defaultBranch = resolveDefaultBranch(repository);
            String remoteUrl = resolveRemoteUrl(repository);
            List<GitRepositorySnapshot.BranchInfo> branches =
                    collectBranches(repository, defaultBranch);
            List<GitRepositorySnapshot.TagInfo> tags = collectTags(repository);
            Map<String, Set<String>> commitBranches =
                    mapCommitBranches(repository, branches, defaultBranch);
            Map<String, Set<String>> commitTags = mapCommitTags(tags);

            List<GitRepositorySnapshot.CommitInfo> commits =
                    collectCommits(repository, commitBranches, commitTags);
            if (commits.size() > analysisProperties.maxCommits()) {
                throw new RepositoryAnalysisException(
                        "LARGE_REPOSITORY",
                        "Repository exceeds configured max commit limit of "
                                + analysisProperties.maxCommits());
            }

            long sizeBytes = directorySize(repositoryPath);
            if (sizeBytes > analysisProperties.maxRepositorySizeBytes()) {
                throw new RepositoryAnalysisException(
                        "LARGE_REPOSITORY", "Repository exceeds configured max size limit");
            }

            String primaryLanguage = languageDetector.detectPrimaryLanguage(repositoryPath);
            String latestSha = commits.isEmpty() ? null : commits.getFirst().sha();
            Instant earliest =
                    commits.stream()
                            .map(GitRepositorySnapshot.CommitInfo::authoredAt)
                            .min(Instant::compareTo)
                            .orElse(null);

            GitRepositorySnapshot snapshot =
                    new GitRepositorySnapshot(
                            repositoryPath.getFileName().toString(),
                            defaultBranch,
                            remoteUrl,
                            commits.size(),
                            sizeBytes,
                            latestSha,
                            earliest,
                            primaryLanguage,
                            branches,
                            tags,
                            commits,
                            repositoryPath);

            log.info(
                    "Git engine collection finish path={} commits={} durationMs={}",
                    repositoryPath,
                    commits.size(),
                    System.currentTimeMillis() - started);
            return snapshot;
        } catch (RepositoryAnalysisException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "GIT_ENGINE_FAILED",
                    "Failed to collect git repository metadata",
                    exception);
        }
    }

    private Path clonePublicGitHubRepository(String sourceUri, Path workspacePath) {
        String normalized = normalizeGitHubUrl(sourceUri);
        Path target = workspacePath.resolve(extractRepositoryName(normalized));
        log.info("Clone start url={} target={}", normalized, target);
        long started = System.currentTimeMillis();
        try {
            Git.cloneRepository()
                    .setURI(normalized)
                    .setDirectory(target.toFile())
                    .setCloneAllBranches(true)
                    .setTimeout(analysisProperties.cloneTimeoutSeconds())
                    .call()
                    .close();
            log.info(
                    "Clone finish url={} durationMs={}",
                    normalized,
                    System.currentTimeMillis() - started);
            return target;
        } catch (GitAPIException exception) {
            String message = exception.getMessage() == null ? "" : exception.getMessage();
            if (message.toLowerCase(Locale.ROOT).contains("not found")
                    || message.toLowerCase(Locale.ROOT).contains("authentication")) {
                throw new RepositoryAnalysisException(
                        "UNSUPPORTED_REPOSITORY",
                        "Only public GitHub repositories are supported",
                        exception);
            }
            if (message.toLowerCase(Locale.ROOT).contains("timed out")
                    || message.toLowerCase(Locale.ROOT).contains("timeout")) {
                throw new RepositoryAnalysisException(
                        HttpStatus.GATEWAY_TIMEOUT,
                        "NETWORK_TIMEOUT",
                        "Cloning the repository timed out",
                        exception);
            }
            throw new RepositoryAnalysisException(
                    HttpStatus.BAD_GATEWAY,
                    "CLONE_FAILURE",
                    "Failed to clone GitHub repository",
                    exception);
        }
    }

    private Path copyValidateLocalRepository(String sourceUri, Path workspacePath) {
        Path source = Path.of(sourceUri).toAbsolutePath().normalize();
        if (!Files.isDirectory(source) || !Files.isDirectory(source.resolve(".git"))) {
            throw new RepositoryAnalysisException(
                    "INVALID_REPOSITORY", "Local path is not a valid git repository: " + sourceUri);
        }
        Path target = workspacePath.resolve(source.getFileName().toString());
        try {
            copyDirectory(source, target);
            validateRepository(openRepository(target));
            return target;
        } catch (IOException exception) {
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CLONE_FAILURE",
                    "Failed to materialize local repository into workspace",
                    exception);
        }
    }

    public String normalizeGitHubUrl(String sourceUri) {
        String trimmed = sourceUri == null ? "" : sourceUri.trim();
        if (trimmed.endsWith(".git")) {
            trimmed = trimmed.substring(0, trimmed.length() - 4);
        }
        if (trimmed.startsWith("git@github.com:")) {
            trimmed = "https://github.com/" + trimmed.substring("git@github.com:".length());
        }
        if (!trimmed.matches("(?i)^https://github\\.com/[\\w.-]+/[\\w.-]+/?$")) {
            throw new RepositoryAnalysisException(
                    "UNSUPPORTED_REPOSITORY",
                    "Only public github.com repository URLs are supported");
        }
        if (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ".git";
    }

    private String extractRepositoryName(String gitUrl) {
        String withoutGit =
                gitUrl.endsWith(".git") ? gitUrl.substring(0, gitUrl.length() - 4) : gitUrl;
        int idx = withoutGit.lastIndexOf('/');
        return idx >= 0 ? withoutGit.substring(idx + 1) : withoutGit;
    }

    private Repository openRepository(Path repositoryPath) throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(repositoryPath.resolve(".git").toFile())
                .readEnvironment()
                .findGitDir()
                .build();
    }

    private void validateRepository(Repository repository) throws IOException {
        if (repository == null || repository.resolve(Constants.HEAD) == null) {
            throw new RepositoryAnalysisException(
                    "INVALID_REPOSITORY", "Repository has no resolvable HEAD");
        }
    }

    private String resolveDefaultBranch(Repository repository) throws IOException {
        String full = repository.getFullBranch();
        if (full != null && full.startsWith("refs/heads/")) {
            return full.substring("refs/heads/".length());
        }
        Ref head = repository.exactRef(Constants.HEAD);
        if (head != null && head.isSymbolic()) {
            String target = head.getTarget().getName();
            if (target.startsWith("refs/heads/")) {
                return target.substring("refs/heads/".length());
            }
        }
        throw new RepositoryAnalysisException("INVALID_BRANCH", "Unable to resolve default branch");
    }

    private String resolveRemoteUrl(Repository repository) {
        var config = repository.getConfig();
        return config.getString("remote", "origin", "url");
    }

    private List<GitRepositorySnapshot.BranchInfo> collectBranches(
            Repository repository, String defaultBranch) throws IOException {
        List<GitRepositorySnapshot.BranchInfo> branches = new ArrayList<>();
        for (Ref ref : repository.getRefDatabase().getRefsByPrefix(Constants.R_HEADS)) {
            String name = Repository.shortenRefName(ref.getName());
            ObjectId objectId = ref.getObjectId();
            branches.add(
                    new GitRepositorySnapshot.BranchInfo(
                            name,
                            name.equals(defaultBranch),
                            objectId == null ? null : objectId.getName()));
        }
        return branches;
    }

    private List<GitRepositorySnapshot.TagInfo> collectTags(Repository repository)
            throws IOException {
        List<GitRepositorySnapshot.TagInfo> tags = new ArrayList<>();
        for (Ref ref : repository.getRefDatabase().getRefsByPrefix(Constants.R_TAGS)) {
            String name = Repository.shortenRefName(ref.getName());
            ObjectId peeled = repository.getRefDatabase().peel(ref).getPeeledObjectId();
            ObjectId objectId = peeled != null ? peeled : ref.getObjectId();
            tags.add(
                    new GitRepositorySnapshot.TagInfo(
                            name, objectId == null ? null : objectId.getName()));
        }
        return tags;
    }

    private Map<String, Set<String>> mapCommitBranches(
            Repository repository,
            List<GitRepositorySnapshot.BranchInfo> branches,
            String defaultBranch)
            throws IOException {
        Map<String, Set<String>> map = new HashMap<>();
        for (GitRepositorySnapshot.BranchInfo branch : branches) {
            if (branch.headCommitSha() != null) {
                map.computeIfAbsent(branch.headCommitSha(), ignored -> new HashSet<>())
                        .add(branch.name());
            }
        }
        ObjectId head = repository.resolve(Constants.HEAD);
        if (head != null) {
            try (RevWalk walk = new RevWalk(repository)) {
                walk.markStart(walk.parseCommit(head));
                for (RevCommit commit : walk) {
                    map.computeIfAbsent(commit.getName(), ignored -> new HashSet<>())
                            .add(defaultBranch);
                }
            }
        }
        return map;
    }

    private Map<String, Set<String>> mapCommitTags(List<GitRepositorySnapshot.TagInfo> tags) {
        Map<String, Set<String>> map = new HashMap<>();
        for (GitRepositorySnapshot.TagInfo tag : tags) {
            if (tag.commitSha() == null) {
                continue;
            }
            map.computeIfAbsent(tag.commitSha(), ignored -> new HashSet<>()).add(tag.name());
        }
        return map;
    }

    private List<GitRepositorySnapshot.CommitInfo> collectCommits(
            Repository repository,
            Map<String, Set<String>> commitBranches,
            Map<String, Set<String>> commitTags)
            throws IOException {
        List<GitRepositorySnapshot.CommitInfo> commits = new ArrayList<>();
        try (RevWalk walk = new RevWalk(repository);
                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            ObjectId head = repository.resolve(Constants.HEAD);
            if (head == null) {
                return commits;
            }
            walk.markStart(walk.parseCommit(head));
            diffFormatter.setRepository(repository);
            diffFormatter.setDiffComparator(RawTextComparator.DEFAULT);
            diffFormatter.setDetectRenames(true);

            for (RevCommit commit : walk) {
                List<String> parents = new ArrayList<>();
                for (RevCommit parent : commit.getParents()) {
                    parents.add(parent.getName());
                }

                int insertions = 0;
                int deletions = 0;
                List<GitRepositorySnapshot.FileChangeInfo> changes = new ArrayList<>();
                if (commit.getParentCount() == 1) {
                    List<DiffEntry> diffs =
                            diffFormatter.scan(commit.getParent(0).getTree(), commit.getTree());
                    for (DiffEntry diff : diffs) {
                        var stats =
                                diffFormatter.toFileHeader(diff).toEditList().stream()
                                        .map(
                                                edit ->
                                                        new int[] {
                                                            edit.getLengthB(), edit.getLengthA()
                                                        })
                                        .reduce(
                                                new int[] {0, 0},
                                                (a, b) -> new int[] {a[0] + b[0], a[1] + b[1]});
                        insertions += stats[0];
                        deletions += stats[1];
                        String path =
                                diff.getNewPath().equals(DiffEntry.DEV_NULL)
                                        ? diff.getOldPath()
                                        : diff.getNewPath();
                        changes.add(
                                new GitRepositorySnapshot.FileChangeInfo(
                                        path, diff.getChangeType().name(), stats[0], stats[1]));
                    }
                } else if (commit.getParentCount() == 0) {
                    // Initial commit: treat all files as additions without expensive full tree
                    // diff.
                    insertions = 0;
                    deletions = 0;
                }

                commits.add(
                        new GitRepositorySnapshot.CommitInfo(
                                commit.getName(),
                                commit.getAuthorIdent().getName(),
                                commit.getAuthorIdent().getEmailAddress(),
                                commit.getAuthorIdent().getWhen().toInstant(),
                                commit.getFullMessage() == null
                                        ? ""
                                        : commit.getFullMessage().trim(),
                                commit.getParentCount() > 1,
                                parents,
                                List.copyOf(
                                        commitBranches.getOrDefault(commit.getName(), Set.of())),
                                List.copyOf(commitTags.getOrDefault(commit.getName(), Set.of())),
                                insertions,
                                deletions,
                                changes));

                if (commits.size() > analysisProperties.maxCommits()) {
                    break;
                }
            }
        }
        return commits;
    }

    private long directorySize(Path root) throws IOException {
        AtomicLong size = new AtomicLong();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .forEach(
                            path -> {
                                try {
                                    size.addAndGet(Files.size(path));
                                } catch (IOException ignored) {
                                    // Skip unreadable files when estimating size.
                                }
                            });
        }
        return size.get();
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        try (Stream<Path> stream = Files.walk(source)) {
            stream.forEach(
                    path -> {
                        try {
                            Path relative = source.relativize(path);
                            Path destination = target.resolve(relative);
                            if (Files.isDirectory(path)) {
                                Files.createDirectories(destination);
                            } else {
                                Files.createDirectories(destination.getParent());
                                Files.copy(path, destination);
                            }
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }
                    });
        } catch (IllegalStateException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
    }
}
