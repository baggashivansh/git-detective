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
@Table(name = "commit_file_changes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitFileChangeEntity {

    @Id private UUID id;

    @Column(name = "commit_id", nullable = false)
    private UUID commitId;

    @Column(nullable = false)
    private String path;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    private FileChangeType changeType;

    @Column(nullable = false)
    private int insertions;

    @Column(nullable = false)
    private int deletions;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
