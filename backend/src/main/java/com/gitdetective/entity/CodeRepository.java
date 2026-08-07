package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "repositories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeRepository {

    @Id private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private RepositorySourceType sourceType;

    @Column(name = "source_uri", nullable = false)
    private String sourceUri;

    @Column(name = "remote_url")
    private String remoteUrl;

    @Column(name = "default_branch")
    private String defaultBranch;

    @Column(name = "total_commits", nullable = false)
    private long totalCommits;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "primary_language")
    private String primaryLanguage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    @Column(name = "status_message")
    private String statusMessage;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "latest_commit_sha")
    private String latestCommitSha;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "analyzed_at")
    private Instant analyzedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = AnalysisStatus.QUEUED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
