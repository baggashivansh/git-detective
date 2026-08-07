package com.gitdetective.ownership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.gitdetective.entity.BusFactorLevel;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.entity.OwnershipKind;
import com.gitdetective.history.FileHistoryEngine;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.CommitJpaRepository;
import java.math.BigDecimal;
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
class OwnershipEngineTest {

    @Mock private CommitJpaRepository commitJpaRepository;

    @Mock private FileHistoryEngine fileHistoryEngine;

    @InjectMocks private OwnershipEngine ownershipEngine;

    @Test
    @DisplayName("calculates ownership percentages and high bus-factor for single owner")
    void calculatesBusFactorHigh() {
        UUID repoId = UUID.randomUUID();
        InvestigationTarget target =
                new InvestigationTarget(
                        InvestigationTargetType.FILE,
                        "file-1",
                        "src/A.java",
                        repoId,
                        UUID.randomUUID(),
                        "src/A.java",
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

        when(fileHistoryEngine.analyze(eq(repoId), eq("src/A.java")))
                .thenReturn(
                        new FileHistoryEngine.FileHistoryResult(
                                "src/A.java",
                                3,
                                1,
                                Map.of("ada@example.com", 3L),
                                List.of(),
                                List.of(),
                                "abc",
                                Instant.parse("2024-01-01T00:00:00Z"),
                                Instant.now(),
                                "Ada <ada@example.com>",
                                "2024-01",
                                List.of(
                                        event("ada@example.com", "Ada"),
                                        event("ada@example.com", "Ada"),
                                        event("ada@example.com", "Ada"))));

        OwnershipEngine.OwnershipResult result = ownershipEngine.calculate(target);

        assertThat(result.owners()).hasSize(1);
        assertThat(result.owners().getFirst().ownershipPercentage())
                .isEqualByComparingTo(new BigDecimal("100.000"));
        assertThat(result.owners().getFirst().ownershipKind()).isEqualTo(OwnershipKind.ACTIVE);
        assertThat(result.busFactorScore()).isEqualTo(1);
        assertThat(result.busFactorLevel()).isEqualTo(BusFactorLevel.HIGH);
        assertThat(result.busFactorExplanation()).contains("50%");
    }

    @Test
    @DisplayName("bus factor increases when ownership is distributed")
    void calculatesDistributedBusFactor() {
        List<OwnershipEngine.OwnerShare> shares =
                List.of(share("a", 40), share("b", 30), share("c", 30));
        assertThat(ownershipEngine.calculateBusFactor(shares)).isEqualTo(2);
    }

    private FileHistoryEngine.FileHistoryEvent event(String email, String name) {
        return new FileHistoryEngine.FileHistoryEvent(
                "sha", Instant.now(), name, email, "msg", "MODIFY", "src/A.java", 10, 2);
    }

    private OwnershipEngine.OwnerShare share(String email, int pct) {
        return new OwnershipEngine.OwnerShare(
                email,
                email,
                pct,
                1,
                10,
                BigDecimal.valueOf(pct),
                OwnershipKind.ACTIVE,
                Instant.now());
    }
}
