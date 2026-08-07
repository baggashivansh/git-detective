package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "investigation_evidence")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationEvidenceEntity {

    @Id private UUID id;

    @Column(name = "investigation_id", nullable = false)
    private UUID investigationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false, length = 64)
    private EvidenceType evidenceType;

    @Column(name = "source_kind", nullable = false, length = 64)
    private String sourceKind;

    @Column(name = "source_ref", nullable = false, length = 1024)
    private String sourceRef;

    @Column(nullable = false, length = 1024)
    private String label;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
