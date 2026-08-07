package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "investigation_impact_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationImpactItemEntity {

    @Id private UUID id;

    @Column(name = "investigation_id", nullable = false)
    private UUID investigationId;

    @Column(name = "item_kind", nullable = false, length = 64)
    private String itemKind;

    @Column(name = "item_ref", nullable = false, length = 1024)
    private String itemRef;

    @Column(name = "item_label", nullable = false, length = 1024)
    private String itemLabel;

    @Column(name = "dependency_depth", nullable = false)
    private int dependencyDepth;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
