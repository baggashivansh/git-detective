package com.gitdetective.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitdetective.assistant.validator.AssistantEvidenceValidator.ValidatedAiResponse;
import com.gitdetective.dto.response.IncidentReportResponse;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.evidence.EvidenceTestFixtures;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IncidentReportCompilerTest {

    private final IncidentReportCompiler compiler = new IncidentReportCompiler();

    @Test
    @DisplayName("compiles timeline, ownership, and blast radius from investigation evidence")
    void compilesFromDetail() {
        InvestigationDetailResponse detail = EvidenceTestFixtures.completedDetail();

        IncidentReportResponse report =
                compiler.compile(
                        "Why did authentication become risky after recent changes?",
                        detail,
                        null,
                        null);

        assertThat(report.insufficientEvidence()).isFalse();
        assertThat(report.confidence()).isGreaterThan(0);
        assertThat(report.timeline()).isNotEmpty();
        assertThat(report.timeline().get(0).occurredAt())
                .isEqualTo(Instant.parse("2026-01-02T00:00:00Z"));
        assertThat(report.timeline().get(0).commitSha()).isEqualTo("abc123");
        assertThat(report.timeline().get(0).strength()).isEqualTo(ClaimStrength.FACT);
        assertThat(report.timelineNote()).contains("not deployment");
        assertThat(report.codeOwnership())
                .extracting("contributorEmail")
                .contains("ada@example.com");
        assertThat(report.blastRadius().score()).isEqualByComparingTo(new BigDecimal("12.000"));
        assertThat(report.blastRadius().note()).contains("dependency graph");
        assertThat(report.riskFactors().stream().anyMatch(item -> item.contains("bus-factor")))
                .isTrue();
        assertThat(report.limitations()).anyMatch(item -> item.contains("Repository timestamps"));
        assertThat(report.claims()).isNotEmpty();
        assertThat(report.finding()).doesNotContain("deployed at");
    }

    @Test
    @DisplayName("reports insufficient evidence when no evidence items exist")
    void insufficientWhenEmpty() {
        Instant now = Instant.parse("2026-03-01T00:00:00Z");
        InvestigationDetailResponse detail =
                new InvestigationDetailResponse(
                        new InvestigationSummaryResponse(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                InvestigationTargetType.BRANCH,
                                "main",
                                "main",
                                InvestigationStatus.COMPLETED,
                                "empty",
                                null,
                                null,
                                null,
                                now,
                                now),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of());

        IncidentReportResponse report = compiler.compile("What broke?", detail, null, null);

        assertThat(report.insufficientEvidence()).isTrue();
        assertThat(report.confidence()).isZero();
        assertThat(report.finding()).isEqualTo(IncidentReportCompiler.INSUFFICIENT);
        assertThat(report.recommendedNextActions()).isNotEmpty();
        assertThat(report.limitations()).anyMatch(item -> item.contains("insufficient"));
    }

    @Test
    @DisplayName("uses validated AI finding but keeps repository facts from evidence")
    void usesValidatedFindingWithoutInventing() {
        ValidatedAiResponse ai =
                new ValidatedAiResponse(
                        "FACT: Demo is the investigation target. HYPOTHESIS: auth risk is unproven.",
                        List.of(),
                        70,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        false);

        IncidentReportResponse report =
                compiler.compile(
                        "Why did authentication become risky?",
                        EvidenceTestFixtures.completedDetail(),
                        null,
                        ai);

        assertThat(report.finding()).contains("Demo is the investigation target");
        assertThat(report.finding()).doesNotContain("nonexistent-commit");
        assertThat(report.timeline())
                .extracting(IncidentReportResponse.TimelineEntry::commitSha)
                .containsExactly("abc123");
        assertThat(report.confidence()).isLessThanOrEqualTo(70);
    }

    @Test
    @DisplayName("does not let an AI insufficient hedge wipe indexed evidence")
    void discardsAiInsufficientWhenEvidenceExists() {
        ValidatedAiResponse ai =
                new ValidatedAiResponse(
                        IncidentReportCompiler.INSUFFICIENT,
                        List.of(),
                        0,
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        true);

        IncidentReportResponse report =
                compiler.compile(
                        "What changed recently?", EvidenceTestFixtures.completedDetail(), null, ai);

        assertThat(report.insufficientEvidence()).isFalse();
        assertThat(report.finding()).isNotEqualTo(IncidentReportCompiler.INSUFFICIENT);
        assertThat(report.finding()).contains("Demo");
        assertThat(report.timeline()).isNotEmpty();
        assertThat(report.confidence()).isGreaterThan(0);
    }

    @Test
    @DisplayName("does not invent authentication claims for unrelated questions")
    void doesNotFrameUnrelatedQuestionsAsAuth() {
        IncidentReportResponse report =
                compiler.compile(
                        "Who owns the parser package?",
                        EvidenceTestFixtures.completedDetail(),
                        null,
                        null);

        assertThat(report.why()).doesNotContain("authentication");
        assertThat(report.riskFactors().stream().anyMatch(item -> item.contains("authentication")))
                .isFalse();
        assertThat(
                        report.recommendedNextActions().stream()
                                .anyMatch(item -> item.toLowerCase().contains("authentication")))
                .isFalse();
    }
}
