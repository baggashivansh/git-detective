package com.gitdetective.ownership;

import com.gitdetective.entity.BusFactorLevel;
import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.OwnershipKind;
import com.gitdetective.history.FileHistoryEngine;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.CommitJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * Deterministic ownership and bus-factor calculation.
 *
 * <p>Bus factor: smallest number of top contributors (by ownership %) whose cumulative share
 * reaches 50%. Levels: 1 = HIGH risk, 2 = MEDIUM, 3+ = LOW.
 */
@Component
@RequiredArgsConstructor
public class OwnershipEngine {

    private final CommitJpaRepository commitJpaRepository;
    private final FileHistoryEngine fileHistoryEngine;

    public OwnershipResult calculate(InvestigationTarget target) {
        Map<String, MutableOwner> owners = new HashMap<>();
        Instant recentCutoff = Instant.now().minus(90, ChronoUnit.DAYS);

        if (target.filePath() != null) {
            FileHistoryEngine.FileHistoryResult history =
                    fileHistoryEngine.analyze(target.repositoryId(), target.filePath());
            for (FileHistoryEngine.FileHistoryEvent event : history.events()) {
                MutableOwner owner =
                        owners.computeIfAbsent(
                                event.authorEmail().toLowerCase(),
                                email -> new MutableOwner(event.authorName(), email));
                owner.totalCommits++;
                owner.linesChanged += event.insertions() + event.deletions();
                if (!event.authoredAt().isBefore(recentCutoff)) {
                    owner.recentCommits++;
                }
                if (owner.lastContributionAt == null
                        || event.authoredAt().isAfter(owner.lastContributionAt)) {
                    owner.lastContributionAt = event.authoredAt();
                }
            }
        } else if (target.contributorEmail() != null) {
            List<CommitEntity> commits =
                    commitJpaRepository
                            .findByRepositoryIdAndAuthorEmailIgnoreCaseOrderByAuthoredAtDesc(
                                    target.repositoryId(), target.contributorEmail());
            for (CommitEntity commit : commits) {
                MutableOwner owner =
                        owners.computeIfAbsent(
                                commit.getAuthorEmail().toLowerCase(),
                                email -> new MutableOwner(commit.getAuthorName(), email));
                owner.totalCommits++;
                owner.linesChanged += commit.getInsertions() + commit.getDeletions();
                if (!commit.getAuthoredAt().isBefore(recentCutoff)) {
                    owner.recentCommits++;
                }
                owner.lastContributionAt = commit.getAuthoredAt();
            }
        } else {
            List<CommitEntity> commits =
                    commitJpaRepository
                            .findByRepositoryIdOrderByAuthoredAtDesc(
                                    target.repositoryId(), PageRequest.of(0, 2000))
                            .getContent();
            for (CommitEntity commit : commits) {
                MutableOwner owner =
                        owners.computeIfAbsent(
                                commit.getAuthorEmail().toLowerCase(),
                                email -> new MutableOwner(commit.getAuthorName(), email));
                owner.totalCommits++;
                owner.linesChanged += commit.getInsertions() + commit.getDeletions();
                if (!commit.getAuthoredAt().isBefore(recentCutoff)) {
                    owner.recentCommits++;
                }
                if (owner.lastContributionAt == null
                        || commit.getAuthoredAt().isAfter(owner.lastContributionAt)) {
                    owner.lastContributionAt = commit.getAuthoredAt();
                }
            }
        }

        long total = owners.values().stream().mapToLong(o -> o.totalCommits).sum();
        List<OwnerShare> shares = new ArrayList<>();
        for (MutableOwner owner : owners.values()) {
            BigDecimal percentage =
                    total == 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(owner.totalCommits * 100.0 / total)
                                    .setScale(3, RoundingMode.HALF_UP);
            OwnershipKind kind;
            if (owner.recentCommits > 0) {
                kind = OwnershipKind.ACTIVE;
            } else if (owner.lastContributionAt != null
                    && owner.lastContributionAt.isBefore(
                            Instant.now().minus(180, ChronoUnit.DAYS))) {
                kind = OwnershipKind.DORMANT;
            } else {
                kind = OwnershipKind.HISTORICAL;
            }
            shares.add(
                    new OwnerShare(
                            owner.name,
                            owner.email,
                            owner.totalCommits,
                            owner.recentCommits,
                            owner.linesChanged,
                            percentage,
                            kind,
                            owner.lastContributionAt));
        }
        shares.sort(Comparator.comparing(OwnerShare::ownershipPercentage).reversed());

        int busFactor = calculateBusFactor(shares);
        BusFactorLevel level =
                busFactor <= 1
                        ? BusFactorLevel.HIGH
                        : busFactor == 2 ? BusFactorLevel.MEDIUM : BusFactorLevel.LOW;
        String explanation =
                "Bus factor is the smallest number of top contributors (by commit ownership"
                        + " percentage) whose cumulative share reaches at least 50%."
                        + " Calculated value="
                        + busFactor
                        + " → risk level "
                        + level
                        + " (1=HIGH, 2=MEDIUM, 3+=LOW).";

        return new OwnershipResult(shares, busFactor, level, explanation);
    }

    int calculateBusFactor(List<OwnerShare> shares) {
        if (shares.isEmpty()) {
            return 0;
        }
        BigDecimal cumulative = BigDecimal.ZERO;
        int count = 0;
        for (OwnerShare share : shares) {
            cumulative = cumulative.add(share.ownershipPercentage());
            count++;
            if (cumulative.compareTo(BigDecimal.valueOf(50)) >= 0) {
                return count;
            }
        }
        return count;
    }

    private static final class MutableOwner {
        private final String name;
        private final String email;
        private long totalCommits;
        private long recentCommits;
        private long linesChanged;
        private Instant lastContributionAt;

        private MutableOwner(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }

    public record OwnerShare(
            String name,
            String email,
            long totalCommits,
            long recentCommits,
            long linesChanged,
            BigDecimal ownershipPercentage,
            OwnershipKind ownershipKind,
            Instant lastContributionAt) {}

    public record OwnershipResult(
            List<OwnerShare> owners,
            int busFactorScore,
            BusFactorLevel busFactorLevel,
            String busFactorExplanation) {}
}
