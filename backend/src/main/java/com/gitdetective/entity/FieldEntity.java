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
@Table(name = "fields")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldEntity {

    @Id private UUID id;

    @Column(name = "type_id", nullable = false)
    private UUID typeId;

    @Column(nullable = false)
    private String name;

    @Column(name = "type_name")
    private String typeName;

    private String visibility;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
