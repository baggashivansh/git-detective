package com.gitdetective.assistant.formatter;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitdetective.assistant.context.EvidenceContextBuilder.EvidenceContext;
import com.gitdetective.assistant.conversation.SuggestedQuestionGenerator;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator.ValidatedAiResponse;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssistantResponseFormatterTest {

    @Test
    @DisplayName("formats citations and follow-up questions")
    void formats() {
        UUID id = UUID.randomUUID();
        EvidenceRecord record =
                EvidenceRecord.builder()
                        .evidenceId(id)
                        .evidenceType(EvidenceCategory.HOTSPOT)
                        .source(EvidenceProvenance.HOTSPOT_DETECTOR)
                        .sourceIdentifier("a.java")
                        .repositoryId(UUID.randomUUID())
                        .investigationId(UUID.randomUUID())
                        .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                        .description("hot")
                        .build();
        EvidenceContext context =
                new EvidenceContext(
                        "inv",
                        "repo",
                        "FILE",
                        "a.java",
                        "overview",
                        "x",
                        List.of(record),
                        List.of("abc"),
                        List.of("a.java"),
                        List.of(),
                        List.of(),
                        95);

        ValidatedAiResponse validated =
                new ValidatedAiResponse(
                        "File is risky due to frequent edits.",
                        List.of(id),
                        90,
                        List.of("a.java"),
                        List.of("abc"),
                        List.of(),
                        List.of(),
                        false);

        var formatted =
                new AssistantResponseFormatter(new SuggestedQuestionGenerator())
                        .format(validated, context, AssistantIntent.HOTSPOT, UUID.randomUUID());

        assertThat(formatted.evidenceUsed()).hasSize(1);
        assertThat(formatted.suggestedFollowUpQuestions()).isNotEmpty();
        assertThat(formatted.confidence()).isEqualTo(90);
    }
}
