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
@Table(name = "investigation_relationships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationRelationshipEntity {

    @Id private UUID id;

    @Column(name = "investigation_id", nullable = false)
    private UUID investigationId;

    @Column(name = "source_key", nullable = false, length = 1024)
    private String sourceKey;

    @Column(name = "source_label", nullable = false, length = 1024)
    private String sourceLabel;

    @Column(name = "source_type", nullable = false, length = 64)
    private String sourceType;

    @Column(name = "target_key", nullable = false, length = 1024)
    private String targetKey;

    @Column(name = "target_label", nullable = false, length = 1024)
    private String targetLabel;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false, length = 64)
    private InvestigationRelationshipType relationshipType;

    @Column(name = "evidence_ref", length = 1024)
    private String evidenceRef;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
