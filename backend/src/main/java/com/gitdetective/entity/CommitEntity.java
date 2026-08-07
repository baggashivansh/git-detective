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
@Table(name = "commits")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitEntity {

    @Id private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(nullable = false, length = 64)
    private String sha;

    @Column(name = "author_name", nullable = false)
    private String authorName;

    @Column(name = "author_email", nullable = false)
    private String authorEmail;

    @Column(name = "authored_at", nullable = false)
    private Instant authoredAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_merge", nullable = false)
    private boolean merge;

    @Column(nullable = false)
    private int insertions;

    @Column(nullable = false)
    private int deletions;

    @Column(name = "files_changed_count", nullable = false)
    private int filesChangedCount;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
