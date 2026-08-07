package com.gitdetective.evidence;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.BusFactorLevel;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.InvestigationRelationshipType;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.entity.OwnershipKind;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.entity.RiskLevel;
import com.gitdetective.entity.TimelineEventType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class EvidenceTestFixtures {

    private EvidenceTestFixtures() {}

    public static final UUID REPO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID INV_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    public static CodeRepository repository() {
        return CodeRepository.builder()
                .id(REPO_ID)
                .name("demo-repo")
                .sourceType(RepositorySourceType.LOCAL)
                .sourceUri("/tmp/demo")
                .defaultBranch("main")
                .primaryLanguage("Java")
                .totalCommits(12)
                .sizeBytes(1024)
                .status(AnalysisStatus.COMPLETED)
                .progressPercent(100)
                .analyzedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
                .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
                .build();
    }

    public static InvestigationDetailResponse completedDetail() {
        Instant t0 = Instant.parse("2026-01-02T00:00:00Z");
        InvestigationSummaryResponse summary =
                new InvestigationSummaryResponse(
                        INV_ID,
                        REPO_ID,
                        InvestigationTargetType.CLASS,
                        "com.example.Demo",
                        "Demo",
                        InvestigationStatus.COMPLETED,
                        "Factual summary",
                        1,
                        BusFactorLevel.HIGH,
                        new BigDecimal("12.000"),
                        t0,
                        t0.plusSeconds(10));

        return new InvestigationDetailResponse(
                summary,
                List.of(
                        new InvestigationDetailResponse.EvidenceItem(
                                UUID.randomUUID(),
                                "FILE",
                                "CLASS",
                                "com.example.Demo",
                                "Investigation target",
                                "target"),
                        new InvestigationDetailResponse.EvidenceItem(
                                UUID.randomUUID(),
                                "DEPENDENCY",
                                "IMPACT",
                                "com.example.Other",
                                "Import edge",
                                "imports Other")),
                List.of(
                        new InvestigationDetailResponse.TimelineItem(
                                UUID.randomUUID(),
                                t0,
                                TimelineEventType.CREATION,
                                "Created",
                                "initial",
                                "Ada",
                                "ada@example.com",
                                "abc123",
                                "abc123")),
                List.of(
                        new InvestigationDetailResponse.OwnershipItem(
                                UUID.randomUUID(),
                                "Ada",
                                "ada@example.com",
                                5,
                                2,
                                100,
                                new BigDecimal("80.00"),
                                OwnershipKind.ACTIVE,
                                t0)),
                List.of(
                        new InvestigationDetailResponse.ImpactItem(
                                UUID.randomUUID(),
                                "CLASS",
                                "com.example.Other",
                                "Other",
                                1,
                                "imports")),
                List.of(
                        new InvestigationDetailResponse.RelationshipItem(
                                UUID.randomUUID(),
                                "com.example.Demo",
                                "Demo",
                                "CLASS",
                                "com.example.Other",
                                "Other",
                                "CLASS",
                                InvestigationRelationshipType.IMPORTS,
                                "import")),
                List.of(
                        new InvestigationDetailResponse.HotspotItem(
                                UUID.randomUUID(),
                                "FILE",
                                "src/Demo.java",
                                "Demo.java",
                                new BigDecimal("9.5"),
                                1,
                                "frequent edits")),
                List.of(
                        new InvestigationDetailResponse.PackageHealthItem(
                                UUID.randomUUID(),
                                "com.example",
                                new BigDecimal("3.2"),
                                4,
                                10,
                                new BigDecimal("1.1"),
                                2,
                                new BigDecimal("0.5"),
                                RiskLevel.MEDIUM)),
                List.of(
                        new InvestigationDetailResponse.CommitClusterItem(
                                UUID.randomUUID(),
                                "feature-x",
                                t0,
                                t0.plusSeconds(3600),
                                3,
                                2,
                                "ada@example.com",
                                "abc123,def456")),
                List.of(
                        new InvestigationDetailResponse.TraceItem(
                                UUID.randomUUID(),
                                "REQUEST_FLOW",
                                1,
                                "DemoController",
                                "com.example.DemoController",
                                "Controller",
                                "mapped")));
    }
}
