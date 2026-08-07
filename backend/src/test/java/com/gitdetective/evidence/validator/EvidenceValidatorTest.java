package com.gitdetective.evidence.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceConfidenceRules;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import com.gitdetective.evidence.model.EvidenceVerificationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EvidenceValidatorTest {

    private final EvidenceValidator validator = new EvidenceValidator();

    @Test
    @DisplayName("marks valid records verified and drops duplicates")
    void validatesAndDedupes() {
        EvidenceRecord a =
                sample(EvidenceTestFixtures.REPO_ID, EvidenceTestFixtures.INV_ID, "ref-1");
        EvidenceRecord duplicate =
                sample(EvidenceTestFixtures.REPO_ID, EvidenceTestFixtures.INV_ID, "ref-1");

        List<EvidenceRecord> result =
                validator.validateAndMark(
                        List.of(a, duplicate),
                        EvidenceTestFixtures.REPO_ID,
                        EvidenceTestFixtures.INV_ID);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().verificationStatus())
                .isEqualTo(EvidenceVerificationStatus.VERIFIED);
    }

    @Test
    @DisplayName("rejects repository mismatch")
    void rejectsRepoMismatch() {
        EvidenceRecord bad = sample(UUID.randomUUID(), EvidenceTestFixtures.INV_ID, "ref");

        assertThatThrownBy(
                        () ->
                                validator.validateAndMark(
                                        List.of(bad),
                                        EvidenceTestFixtures.REPO_ID,
                                        EvidenceTestFixtures.INV_ID))
                .isInstanceOf(EvidenceValidationException.class)
                .hasMessageContaining("repository mismatch");
    }

    @Test
    @DisplayName("rejects confidence mismatch")
    void rejectsConfidenceMismatch() {
        EvidenceRecord bad =
                EvidenceRecord.builder()
                        .evidenceType(EvidenceCategory.TIMELINE)
                        .source(EvidenceProvenance.TIMELINE_ENGINE)
                        .sourceIdentifier("sha")
                        .repositoryId(EvidenceTestFixtures.REPO_ID)
                        .investigationId(EvidenceTestFixtures.INV_ID)
                        .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                        .confidence(EvidenceConfidenceRules.RELATIONSHIP_GRAPH)
                        .description("event")
                        .build();

        assertThatThrownBy(
                        () ->
                                validator.validateAndMark(
                                        List.of(bad),
                                        EvidenceTestFixtures.REPO_ID,
                                        EvidenceTestFixtures.INV_ID))
                .isInstanceOf(EvidenceValidationException.class)
                .hasMessageContaining("confidence mismatch");
    }

    @Test
    @DisplayName("rejects missing source identifier")
    void rejectsMissingData() {
        EvidenceRecord bad =
                EvidenceRecord.builder()
                        .evidenceType(EvidenceCategory.FILE)
                        .source(EvidenceProvenance.GIT_COMMIT)
                        .sourceIdentifier(" ")
                        .repositoryId(EvidenceTestFixtures.REPO_ID)
                        .investigationId(EvidenceTestFixtures.INV_ID)
                        .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                        .description("file")
                        .build();

        assertThatThrownBy(
                        () ->
                                validator.validateAndMark(
                                        List.of(bad),
                                        EvidenceTestFixtures.REPO_ID,
                                        EvidenceTestFixtures.INV_ID))
                .isInstanceOf(EvidenceValidationException.class)
                .hasMessageContaining("sourceIdentifier");
    }

    private static EvidenceRecord sample(UUID repoId, UUID invId, String ref) {
        return EvidenceRecord.builder()
                .evidenceType(EvidenceCategory.TIMELINE)
                .source(EvidenceProvenance.TIMELINE_ENGINE)
                .sourceIdentifier(ref)
                .repositoryId(repoId)
                .investigationId(invId)
                .timestamp(Instant.parse("2026-01-01T00:00:00Z"))
                .description("Created: " + ref)
                .build();
    }
}
