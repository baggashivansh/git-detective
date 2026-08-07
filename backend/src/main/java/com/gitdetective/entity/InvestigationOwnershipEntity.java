package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "investigation_ownership")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationOwnershipEntity {

    @Id private UUID id;

    @Column(name = "investigation_id", nullable = false)
    private UUID investigationId;

    @Column(name = "contributor_email", nullable = false, length = 320)
    private String contributorEmail;

    @Column(name = "contributor_name", nullable = false, length = 255)
    private String contributorName;

    @Column(name = "total_commits", nullable = false)
    private long totalCommits;

    @Column(name = "recent_commits", nullable = false)
    private long recentCommits;

    @Column(name = "lines_changed", nullable = false)
    private long linesChanged;

    @Column(name = "ownership_percentage", nullable = false, precision = 8, scale = 3)
    private BigDecimal ownershipPercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_kind", nullable = false, length = 32)
    private OwnershipKind ownershipKind;

    @Column(name = "last_contribution_at")
    private Instant lastContributionAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
