package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "investigation_package_health")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationPackageHealthEntity {

    @Id private UUID id;

    @Column(name = "investigation_id", nullable = false)
    private UUID investigationId;

    @Column(name = "package_name", nullable = false, length = 1024)
    private String packageName;

    @Column(name = "complexity_score", nullable = false, precision = 10, scale = 3)
    private BigDecimal complexityScore;

    @Column(name = "dependency_count", nullable = false)
    private int dependencyCount;

    @Column(name = "package_size", nullable = false)
    private int packageSize;

    @Column(name = "modification_frequency", nullable = false, precision = 10, scale = 3)
    private BigDecimal modificationFrequency;

    @Column(name = "contributor_count", nullable = false)
    private int contributorCount;

    @Column(name = "growth_score", nullable = false, precision = 10, scale = 3)
    private BigDecimal growthScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 16)
    private RiskLevel riskLevel;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
