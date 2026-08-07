package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "language_statistics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageStatisticEntity {

    @Id private UUID id;

    @Column(name = "repository_id", nullable = false)
    private UUID repositoryId;

    @Column(nullable = false)
    private String language;

    @Column(name = "file_count", nullable = false)
    private int fileCount;

    @Column(name = "line_count", nullable = false)
    private long lineCount;

    @Column(name = "byte_count", nullable = false)
    private long byteCount;

    @Column(nullable = false, precision = 6, scale = 3)
    private BigDecimal percentage;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (percentage == null) {
            percentage = BigDecimal.ZERO;
        }
    }
}
