package com.gitdetective.timeline;

import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.TagEntity;
import com.gitdetective.entity.TimelineEventType;
import com.gitdetective.history.FileHistoryEngine;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.TagJpaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TimelineEngine {

    private final FileHistoryEngine fileHistoryEngine;
    private final CommitJpaRepository commitJpaRepository;
    private final TagJpaRepository tagJpaRepository;

    public List<TimelineEvent> build(InvestigationTarget target) {
        List<TimelineEvent> events = new ArrayList<>();

        if (target.filePath() != null) {
            FileHistoryEngine.FileHistoryResult history =
                    fileHistoryEngine.analyze(target.repositoryId(), target.filePath());
            boolean first = true;
            for (FileHistoryEngine.FileHistoryEvent event : history.events()) {
                TimelineEventType type =
                        first
                                ? TimelineEventType.CREATION
                                : switch (event.changeType()) {
                                    case "RENAME" -> TimelineEventType.RENAME;
                                    case "DELETE" -> TimelineEventType.MODIFICATION;
                                    default -> TimelineEventType.MODIFICATION;
                                };
                if ("RENAME".equals(event.changeType())
                        && !event.path().equals(target.filePath())) {
                    type = TimelineEventType.MOVE;
                }
                events.add(
                        new TimelineEvent(
                                event.authoredAt(),
                                type,
                                first ? "File created" : "File modified",
                                event.message(),
                                event.authorName(),
                                event.authorEmail(),
                                event.sha(),
                                "commit:" + event.sha()));
                first = false;
            }
        }

        if (target.commitSha() != null && target.filePath() == null) {
            commitJpaRepository
                    .findByRepositoryIdAndSha(target.repositoryId(), target.commitSha())
                    .ifPresent(
                            commit ->
                                    events.add(
                                            eventFromCommit(
                                                    commit,
                                                    commit.isMerge()
                                                            ? TimelineEventType.MERGE
                                                            : TimelineEventType.MODIFICATION,
                                                    "Target commit")));
        }

        if (target.contributorEmail() != null && target.filePath() == null) {
            List<CommitEntity> commits =
                    commitJpaRepository
                            .findByRepositoryIdAndAuthorEmailIgnoreCaseOrderByAuthoredAtDesc(
                                    target.repositoryId(), target.contributorEmail());
            for (CommitEntity commit : commits) {
                events.add(
                        eventFromCommit(
                                commit,
                                TimelineEventType.CONTRIBUTOR_CHANGE,
                                "Contributor commit"));
            }
        }

        if (target.branchName() != null) {
            events.add(
                    new TimelineEvent(
                            null,
                            TimelineEventType.BRANCH_MERGE,
                            "Branch head observed",
                            "Branch " + target.branchName() + " head=" + target.commitSha(),
                            null,
                            null,
                            target.commitSha(),
                            "branch:" + target.branchName()));
        }

        for (TagEntity tag : tagJpaRepository.findByRepositoryId(target.repositoryId())) {
            boolean relevant =
                    target.tagName() != null && target.tagName().equals(tag.getName())
                            || (target.commitSha() != null
                                    && target.commitSha().equals(tag.getCommitSha()))
                            || target.filePath() != null;
            if (!relevant && target.tagName() == null && target.filePath() == null) {
                continue;
            }
            if (target.tagName() != null && !target.tagName().equals(tag.getName())) {
                continue;
            }
            events.add(
                    new TimelineEvent(
                            null,
                            TimelineEventType.TAG_APPEARANCE,
                            "Tag " + tag.getName(),
                            "Tag points to " + tag.getCommitSha(),
                            null,
                            null,
                            tag.getCommitSha(),
                            "tag:" + tag.getName()));
        }

        if (events.isEmpty()) {
            List<CommitEntity> recent =
                    commitJpaRepository
                            .findByRepositoryIdOrderByAuthoredAtDesc(
                                    target.repositoryId(), PageRequest.of(0, 20))
                            .getContent();
            for (CommitEntity commit : recent) {
                events.add(
                        eventFromCommit(
                                commit,
                                commit.isMerge()
                                        ? TimelineEventType.MERGE
                                        : TimelineEventType.MODIFICATION,
                                "Repository commit"));
            }
        }

        events.sort(
                Comparator.comparing(
                        TimelineEvent::occurredAt,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        return events;
    }

    private TimelineEvent eventFromCommit(
            CommitEntity commit, TimelineEventType type, String title) {
        return new TimelineEvent(
                commit.getAuthoredAt(),
                type,
                title,
                commit.getMessage(),
                commit.getAuthorName(),
                commit.getAuthorEmail(),
                commit.getSha(),
                "commit:" + commit.getSha());
    }

    public record TimelineEvent(
            java.time.Instant occurredAt,
            TimelineEventType eventType,
            String title,
            String detail,
            String actorName,
            String actorEmail,
            String commitSha,
            String evidenceRef) {}
}
