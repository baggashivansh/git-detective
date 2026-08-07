package com.gitdetective.assistant.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.assistant.context.EvidenceContextBuilder.EvidenceContext;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssistantEvidenceValidatorTest {

    private final AssistantEvidenceValidator validator =
            new AssistantEvidenceValidator(new ObjectMapper());

    @Test
    @DisplayName("accepts responses that cite known evidence ids")
    void acceptsValid() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        EvidenceRecord record =
                EvidenceRecord.builder()
                        .evidenceId(id)
                        .evidenceType(EvidenceCategory.OWNERSHIP)
                        .source(EvidenceProvenance.OWNERSHIP_ENGINE)
                        .sourceIdentifier("ada@example.com")
                        .repositoryId(UUID.randomUUID())
                        .investigationId(UUID.randomUUID())
                        .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                        .description("owner")
                        .build();
        EvidenceContext context =
                new EvidenceContext(
                        "inv",
                        "repo",
                        "FILE",
                        "a.java",
                        "overview",
                        "[id=" + id + " type=OWNERSHIP] owner",
                        List.of(record),
                        List.of(),
                        List.of("a.java"),
                        List.of(),
                        List.of("ada@example.com"),
                        100);

        String raw =
                """
                {"answer":"Ada owns it.","evidenceIds":["aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"],"confidence":95,
                "referencedFiles":["a.java"],"referencedCommits":[],"referencedContributors":["ada@example.com"],"referencedPackages":[]}
                """;

        var validated = validator.validate(raw, context);
        assertThat(validated.evidenceIds()).containsExactly(id);
        assertThat(validated.confidence()).isEqualTo(95);
    }

    @Test
    @DisplayName("rejects unknown evidence ids")
    void rejectsUnknownEvidence() {
        EvidenceContext context =
                new EvidenceContext(
                        "inv",
                        "repo",
                        "FILE",
                        "a.java",
                        "overview",
                        "",
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        0);

        String raw =
                """
                {"answer":"Invented.","evidenceIds":["bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"],"confidence":50,
                "referencedFiles":[],"referencedCommits":[],"referencedContributors":[],"referencedPackages":[]}
                """;

        assertThatThrownBy(() -> validator.validate(raw, context))
                .isInstanceOf(AssistantValidationException.class);
    }
}
