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
@Table(name = "code_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeTypeEntity {

    @Id private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(name = "package_id")
    private UUID packageId;

    @Column(name = "file_id")
    private UUID fileId;

    @Column(nullable = false)
    private String name;

    @Column(name = "fully_qualified_name", nullable = false)
    private String fullyQualifiedName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CodeTypeKind kind;

    private String visibility;

    @Column(name = "superclass_name")
    private String superclassName;

    @Column(name = "start_line")
    private Integer startLine;

    @Column(name = "end_line")
    private Integer endLine;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
