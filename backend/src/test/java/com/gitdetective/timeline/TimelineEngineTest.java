package com.gitdetective.timeline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.entity.TimelineEventType;
import com.gitdetective.history.FileHistoryEngine;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.TagJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimelineEngineTest {

    @Mock private FileHistoryEngine fileHistoryEngine;

    @Mock private CommitJpaRepository commitJpaRepository;

    @Mock private TagJpaRepository tagJpaRepository;

    @InjectMocks private TimelineEngine timelineEngine;

    @Test
    @DisplayName("builds creation and modification timeline from file history evidence")
    void buildsFileTimeline() {
        UUID repoId = UUID.randomUUID();
        InvestigationTarget target =
                new InvestigationTarget(
                        InvestigationTargetType.FILE,
                        "f1",
                        "A.java",
                        repoId,
                        UUID.randomUUID(),
                        "A.java",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        when(fileHistoryEngine.analyze(eq(repoId), eq("A.java")))
                .thenReturn(
                        new FileHistoryEngine.FileHistoryResult(
                                "A.java",
                                2,
                                1,
                                Map.of("a@x.com", 2L),
                                List.of(),
                                List.of(),
                                "sha1",
                                Instant.parse("2024-01-01T00:00:00Z"),
                                Instant.parse("2024-02-01T00:00:00Z"),
                                "Ada",
                                "2024-01",
                                List.of(
                                        new FileHistoryEngine.FileHistoryEvent(
                                                "sha1",
                                                Instant.parse("2024-01-01T00:00:00Z"),
                                                "Ada",
                                                "a@x.com",
                                                "create",
                                                "ADD",
                                                "A.java",
                                                10,
                                                0),
                                        new FileHistoryEngine.FileHistoryEvent(
                                                "sha2",
                                                Instant.parse("2024-02-01T00:00:00Z"),
                                                "Ada",
                                                "a@x.com",
                                                "update",
                                                "MODIFY",
                                                "A.java",
                                                3,
                                                1))));
        when(tagJpaRepository.findByRepositoryId(repoId)).thenReturn(List.of());

        List<TimelineEngine.TimelineEvent> events = timelineEngine.build(target);

        assertThat(events).hasSize(2);
        assertThat(events.getFirst().eventType()).isEqualTo(TimelineEventType.CREATION);
        assertThat(events.get(1).eventType()).isEqualTo(TimelineEventType.MODIFICATION);
        assertThat(events.getFirst().evidenceRef()).startsWith("commit:");
    }
}
