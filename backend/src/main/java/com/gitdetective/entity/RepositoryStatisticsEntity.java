package com.gitdetective.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "repository_statistics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryStatisticsEntity {

    @Id
    @Column(name = "repository_id")
    private UUID repositoryId;

    @Column(name = "total_files", nullable = false)
    private long totalFiles;

    @Column(name = "total_directories", nullable = false)
    private long totalDirectories;

    @Column(name = "total_lines", nullable = false)
    private long totalLines;

    @Column(name = "total_packages", nullable = false)
    private long totalPackages;

    @Column(name = "total_classes", nullable = false)
    private long totalClasses;

    @Column(name = "total_interfaces", nullable = false)
    private long totalInterfaces;

    @Column(name = "total_enums", nullable = false)
    private long totalEnums;

    @Column(name = "total_methods", nullable = false)
    private long totalMethods;

    @Column(name = "total_contributors", nullable = false)
    private long totalContributors;

    @Column(name = "total_branches", nullable = false)
    private long totalBranches;

    @Column(name = "total_tags", nullable = false)
    private long totalTags;

    @Column(name = "binary_file_count", nullable = false)
    private long binaryFileCount;

    @Column(name = "ignored_file_count", nullable = false)
    private long ignoredFileCount;
}
