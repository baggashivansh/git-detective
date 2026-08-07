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
@Table(name = "investigation_traces")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestigationTraceEntity {

    @Id private UUID id;

    @Column(name = "investigation_id", nullable = false)
    private UUID investigationId;

    @Column(name = "trace_kind", nullable = false, length = 64)
    private String traceKind;

    @Column(name = "step_order", nullable = false)
    private int stepOrder;

    @Column(name = "step_label", nullable = false, length = 1024)
    private String stepLabel;

    @Column(name = "step_ref", length = 1024)
    private String stepRef;

    @Column(name = "evidence_ref", length = 1024)
    private String evidenceRef;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
