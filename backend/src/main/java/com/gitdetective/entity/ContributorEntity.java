package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "contributors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributorEntity {

    @Id private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "commit_count", nullable = false)
    private long commitCount;

    @Column(name = "files_modified", nullable = false)
    private long filesModified;

    @Column(name = "lines_added", nullable = false)
    private long linesAdded;

    @Column(name = "lines_deleted", nullable = false)
    private long linesDeleted;

    @Column(name = "last_contribution_at")
    private Instant lastContributionAt;

    @Column(name = "contribution_percentage", nullable = false, precision = 6, scale = 3)
    private BigDecimal contributionPercentage;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (contributionPercentage == null) {
            contributionPercentage = BigDecimal.ZERO;
        }
    }
}
