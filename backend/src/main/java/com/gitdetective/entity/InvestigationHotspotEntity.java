package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "investigation_hotspots")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationHotspotEntity {

    @Id private UUID id;

    @Column(name = "investigation_id", nullable = false)
    private UUID investigationId;

    @Column(name = "hotspot_kind", nullable = false, length = 64)
    private String hotspotKind;

    @Column(name = "item_ref", nullable = false, length = 1024)
    private String itemRef;

    @Column(name = "item_label", nullable = false, length = 1024)
    private String itemLabel;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal score;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
