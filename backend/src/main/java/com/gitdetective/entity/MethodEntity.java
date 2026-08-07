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
@Table(name = "methods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodEntity {

    @Id private UUID id;

    @Column(name = "type_id", nullable = false)
    private UUID typeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String signature;

    @Column(name = "return_type")
    private String returnType;

    private String visibility;

    @Column(name = "is_constructor", nullable = false)
    private boolean constructor;

    @Column(name = "parameter_count", nullable = false)
    private int parameterCount;

    @Column(name = "start_line")
    private Integer startLine;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
