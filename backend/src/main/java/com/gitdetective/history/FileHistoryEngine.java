package com.gitdetective.history;

import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.CommitFileChangeEntity;
import com.gitdetective.entity.FileChangeType;
import com.gitdetective.repository.CommitFileChangeJpaRepository;
import com.gitdetective.repository.CommitJpaRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileHistoryEngine {

    private final CommitJpaRepository commitJpaRepository;
    private final CommitFileChangeJpaRepository commitFileChangeJpaRepository;

    public FileHistoryResult analyze(UUID repositoryId, String filePath) {
        List<CommitEntity> commits =
                commitJpaRepository
                        .findByRepositoryIdOrderByAuthoredAtDesc(
                                repositoryId, PageRequest.of(0, 5000))
                        .getContent();
        Map<UUID, CommitEntity> commitById =
                commits.stream().collect(Collectors.toMap(CommitEntity::getId, c -> c));

        List<CommitFileChangeEntity> relevant = new ArrayList<>();
        for (CommitEntity commit : commits) {
            for (CommitFileChangeEntity change :
                    commitFileChangeJpaRepository.findByCommitId(commit.getId())) {
                if (matchesPath(filePath, change.getPath())) {
                    relevant.add(change);
                }
            }
        }

        relevant.sort(
                Comparator.comparing(
                        change -> commitById.get(change.getCommitId()).getAuthoredAt()));

        Map<String, Long> authors = new HashMap<>();
        List<String> renames = new ArrayList<>();
        List<String> moves = new ArrayList<>();
        Instant first = null;
        Instant last = null;
        String creationSha = null;
        String lastModifier = null;
        Map<String, Long> monthBuckets = new HashMap<>();

        for (CommitFileChangeEntity change : relevant) {
            CommitEntity commit = commitById.get(change.getCommitId());
            authors.merge(commit.getAuthorEmail(), 1L, Long::sum);
            if (first == null) {
                first = commit.getAuthoredAt();
                creationSha = commit.getSha();
            }
            last = commit.getAuthoredAt();
            lastModifier = commit.getAuthorName() + " <" + commit.getAuthorEmail() + ">";
            String month =
                    DateTimeFormatter.ofPattern("yyyy-MM")
                            .withZone(ZoneOffset.UTC)
                            .format(commit.getAuthoredAt());
            monthBuckets.merge(month, 1L, Long::sum);

            if (change.getChangeType() == FileChangeType.RENAME) {
                renames.add(commit.getSha() + ": " + change.getPath());
            }
            if (change.getChangeType() == FileChangeType.RENAME
                    || looksLikeMove(filePath, change.getPath())) {
                moves.add(commit.getSha() + ": " + change.getPath());
            }
        }

        String mostActivePeriod =
                monthBuckets.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);

        Set<String> uniqueAuthors = new HashSet<>(authors.keySet());
        return new FileHistoryResult(
                filePath,
                relevant.size(),
                uniqueAuthors.size(),
                authors,
                renames,
                moves,
                creationSha,
                first,
                last,
                lastModifier,
                mostActivePeriod,
                relevant.stream()
                        .map(
                                change -> {
                                    CommitEntity commit = commitById.get(change.getCommitId());
                                    return new FileHistoryEvent(
                                            commit.getSha(),
                                            commit.getAuthoredAt(),
                                            commit.getAuthorName(),
                                            commit.getAuthorEmail(),
                                            commit.getMessage(),
                                            change.getChangeType().name(),
                                            change.getPath(),
                                            change.getInsertions(),
                                            change.getDeletions());
                                })
                        .toList());
    }

    private boolean matchesPath(String target, String changePath) {
        if (target == null || changePath == null) {
            return false;
        }
        return changePath.equals(target)
                || changePath.endsWith("/" + target)
                || target.endsWith("/" + changePath)
                || basename(target).equals(basename(changePath));
    }

    private boolean looksLikeMove(String target, String changePath) {
        return basename(target).equals(basename(changePath)) && !target.equals(changePath);
    }

    private String basename(String path) {
        int idx = path.lastIndexOf('/');
        return idx >= 0 ? path.substring(idx + 1) : path;
    }

    public record FileHistoryResult(
            String filePath,
            int modificationCount,
            int authorCount,
            Map<String, Long> authors,
            List<String> renameHistory,
            List<String> moveHistory,
            String creationCommitSha,
            Instant createdAt,
            Instant lastModifiedAt,
            String lastModifier,
            String mostActivePeriod,
            List<FileHistoryEvent> events) {}

    public record FileHistoryEvent(
            String sha,
            Instant authoredAt,
            String authorName,
            String authorEmail,
            String message,
            String changeType,
            String path,
            int insertions,
            int deletions) {}
}
