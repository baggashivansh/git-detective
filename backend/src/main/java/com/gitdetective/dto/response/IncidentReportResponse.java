package com.gitdetective.dto.response;

import com.gitdetective.incident.ClaimStrength;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record IncidentReportResponse(
        String finding,
        int confidence,
        String why,
        String timelineNote,
        List<TimelineEntry> timeline,
        List<EvidenceEntry> keyEvidence,
        List<String> affectedFiles,
        List<String> affectedComponents,
        List<OwnershipEntry> codeOwnership,
        BlastRadius blastRadius,
        List<String> riskFactors,
        List<String> recommendedNextActions,
        List<String> limitations,
        List<Claim> claims,
        boolean insufficientEvidence) {

    public record TimelineEntry(
            Instant occurredAt,
            String title,
            String detail,
            String commitSha,
            ClaimStrength strength) {}

    public record EvidenceEntry(
            UUID evidenceId, String label, String detail, ClaimStrength strength) {}

    public record OwnershipEntry(
            String contributorName,
            String contributorEmail,
            BigDecimal ownershipPercentage,
            String ownershipKind) {}

    public record BlastRadius(BigDecimal score, List<String> items, String note) {}

    public record Claim(String statement, ClaimStrength strength, List<UUID> evidenceIds) {}
}
