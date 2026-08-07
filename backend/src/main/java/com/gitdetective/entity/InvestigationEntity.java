package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
@Table(name = "investigations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationEntity {

    @Id private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 32)
    private InvestigationTargetType targetType;

    @Column(name = "target_ref", nullable = false, length = 1024)
    private String targetRef;

    @Column(name = "target_label", nullable = false, length = 1024)
    private String targetLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvestigationStatus status;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "bus_factor_score")
    private Integer busFactorScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "bus_factor_level", length = 16)
    private BusFactorLevel busFactorLevel;

    @Column(name = "blast_radius_score", precision = 8, scale = 3)
    private BigDecimal blastRadiusScore;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = InvestigationStatus.QUEUED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
