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
@Table(name = "file_exports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileExportEntity {

    @Id private UUID id;

    @Column(name = "file_id", nullable = false)
    private UUID fileId;

    @Column(name = "export_name", nullable = false)
    private String exportName;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
