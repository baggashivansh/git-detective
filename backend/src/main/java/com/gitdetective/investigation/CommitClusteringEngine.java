package com.gitdetective.investigation;

import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.CommitFileChangeEntity;
import com.gitdetective.repository.CommitFileChangeJpaRepository;
import com.gitdetective.repository.CommitJpaRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Groups commits into logical clusters using time proximity, shared files, and shared contributors.
 */
@Component
@RequiredArgsConstructor
public class CommitClusteringEngine {

    private static final Duration PROXIMITY = Duration.ofHours(12);

    private final CommitJpaRepository commitJpaRepository;
    private final CommitFileChangeJpaRepository commitFileChangeJpaRepository;

    public List<CommitCluster> cluster(UUID repositoryId) {
        List<CommitEntity> commits =
                new ArrayList<>(
                        commitJpaRepository
                                .findByRepositoryIdOrderByAuthoredAtDesc(
                                        repositoryId, PageRequest.of(0, 500))
                                .getContent());
        commits.sort((a, b) -> a.getAuthoredAt().compareTo(b.getAuthoredAt()));

        List<CommitCluster> clusters = new ArrayList<>();
        List<CommitEntity> current = new ArrayList<>();
        Set<String> currentFiles = new HashSet<>();
        Set<String> currentAuthors = new HashSet<>();

        for (CommitEntity commit : commits) {
            Set<String> files =
                    commitFileChangeJpaRepository.findByCommitId(commit.getId()).stream()
                            .map(CommitFileChangeEntity::getPath)
                            .collect(Collectors.toSet());
            boolean joins = false;
            if (!current.isEmpty()) {
                CommitEntity last = current.getLast();
                boolean timeClose =
                        Duration.between(last.getAuthoredAt(), commit.getAuthoredAt())
                                        .abs()
                                        .compareTo(PROXIMITY)
                                <= 0;
                boolean sharedAuthor = currentAuthors.contains(commit.getAuthorEmail());
                boolean sharedFile = files.stream().anyMatch(currentFiles::contains);
                joins = timeClose && (sharedAuthor || sharedFile);
            }

            if (!joins && !current.isEmpty()) {
                clusters.add(toCluster(current, currentFiles, currentAuthors));
                current = new ArrayList<>();
                currentFiles = new HashSet<>();
                currentAuthors = new HashSet<>();
            }

            current.add(commit);
            currentFiles.addAll(files);
            currentAuthors.add(commit.getAuthorEmail());
        }
        if (!current.isEmpty()) {
            clusters.add(toCluster(current, currentFiles, currentAuthors));
        }
        return clusters;
    }

    private CommitCluster toCluster(
            List<CommitEntity> commits, Set<String> files, Set<String> authors) {
        Instant start = commits.getFirst().getAuthoredAt();
        Instant end = commits.getLast().getAuthoredAt();
        String label =
                "Cluster "
                        + commits.getFirst().getSha().substring(0, 7)
                        + "…"
                        + commits.getLast().getSha().substring(0, 7);
        return new CommitCluster(
                label,
                start,
                end,
                commits.size(),
                files.size(),
                String.join(",", authors),
                commits.stream().map(CommitEntity::getSha).collect(Collectors.joining(",")));
    }

    public record CommitCluster(
            String label,
            Instant startAt,
            Instant endAt,
            int commitCount,
            int sharedFiles,
            String contributors,
            String commitShas) {}
}
