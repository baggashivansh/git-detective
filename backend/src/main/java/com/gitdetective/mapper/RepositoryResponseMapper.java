package com.gitdetective.mapper;

import com.gitdetective.dto.response.CodeTypeResponse;
import com.gitdetective.dto.response.CommitResponse;
import com.gitdetective.dto.response.ContributorResponse;
import com.gitdetective.dto.response.LanguageStatisticResponse;
import com.gitdetective.dto.response.PackageResponse;
import com.gitdetective.dto.response.RepositoryStatisticsResponse;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.ContributorEntity;
import com.gitdetective.entity.LanguageStatisticEntity;
import com.gitdetective.entity.PackageEntity;
import com.gitdetective.entity.RepositoryStatisticsEntity;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class RepositoryResponseMapper {

    public RepositorySummaryResponse toSummary(CodeRepository repository) {
        return new RepositorySummaryResponse(
                repository.getId(),
                repository.getName(),
                repository.getSourceType(),
                repository.getSourceUri(),
                repository.getRemoteUrl(),
                repository.getDefaultBranch(),
                repository.getTotalCommits(),
                repository.getSizeBytes(),
                repository.getPrimaryLanguage(),
                repository.getStatus(),
                repository.getStatusMessage(),
                repository.getProgressPercent(),
                repository.getErrorCode(),
                repository.getErrorMessage(),
                repository.getLatestCommitSha(),
                repository.getCreatedAt(),
                repository.getUpdatedAt(),
                repository.getAnalyzedAt());
    }

    public ContributorResponse toContributor(ContributorEntity entity) {
        return new ContributorResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getCommitCount(),
                entity.getFilesModified(),
                entity.getLinesAdded(),
                entity.getLinesDeleted(),
                entity.getLastContributionAt(),
                entity.getContributionPercentage());
    }

    public LanguageStatisticResponse toLanguage(LanguageStatisticEntity entity) {
        return new LanguageStatisticResponse(
                entity.getLanguage(),
                entity.getFileCount(),
                entity.getLineCount(),
                entity.getByteCount(),
                entity.getPercentage());
    }

    public CommitResponse toCommit(
            CommitEntity entity, List<String> parents, List<String> branches, List<String> tags) {
        return new CommitResponse(
                entity.getId(),
                entity.getSha(),
                entity.getAuthorName(),
                entity.getAuthorEmail(),
                entity.getAuthoredAt(),
                entity.getMessage(),
                entity.isMerge(),
                entity.getInsertions(),
                entity.getDeletions(),
                entity.getFilesChangedCount(),
                parents,
                branches,
                tags);
    }

    public PackageResponse toPackage(PackageEntity entity) {
        return new PackageResponse(
                entity.getId(), entity.getName(), entity.getPath(), entity.getFileCount());
    }

    public CodeTypeResponse toCodeType(CodeTypeEntity entity, String packageName) {
        return new CodeTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getFullyQualifiedName(),
                entity.getKind(),
                entity.getVisibility(),
                entity.getSuperclassName(),
                packageName);
    }

    public RepositoryStatisticsResponse toStatistics(
            RepositoryStatisticsEntity stats, CodeRepository repository) {
        return new RepositoryStatisticsResponse(
                stats.getRepositoryId(),
                stats.getTotalFiles(),
                stats.getTotalDirectories(),
                stats.getTotalLines(),
                stats.getTotalPackages(),
                stats.getTotalClasses(),
                stats.getTotalInterfaces(),
                stats.getTotalEnums(),
                stats.getTotalMethods(),
                stats.getTotalContributors(),
                stats.getTotalBranches(),
                stats.getTotalTags(),
                stats.getBinaryFileCount(),
                stats.getIgnoredFileCount(),
                repository.getTotalCommits(),
                repository.getSizeBytes());
    }
}
