package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntity {

    @Id private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String name;

    @Column(name = "parent_path")
    private String parentPath;

    private String extension;
    private String language;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "line_count", nullable = false)
    private int lineCount;

    @Column(name = "is_directory", nullable = false)
    private boolean directory;

    @Column(name = "is_binary", nullable = false)
    private boolean binary;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Column(name = "is_ignored", nullable = false)
    private boolean ignored;

    @Column(name = "content_hash")
    private String contentHash;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "method_count", nullable = false)
    private int methodCount;

    @Column(name = "field_count", nullable = false)
    private int fieldCount;

    @Column(name = "import_count", nullable = false)
    private int importCount;

    @Column(name = "export_count", nullable = false)
    private int exportCount;

    @Column(name = "created_at_fs")
    private Instant createdAtFs;

    @Column(name = "modified_at_fs")
    private Instant modifiedAtFs;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
