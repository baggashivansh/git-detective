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
@Table(name = "file_imports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileImportEntity {

    @Id private UUID id;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "import_name", nullable = false)
    private String importName;

    @Column(name = "is_static", nullable = false)
    private boolean staticImport;

    @Column(name = "is_asterisk", nullable = false)
    private boolean asterisk;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
