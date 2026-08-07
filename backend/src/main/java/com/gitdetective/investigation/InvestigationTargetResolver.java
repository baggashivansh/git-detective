package com.gitdetective.investigation;

import com.gitdetective.entity.BranchEntity;
import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.ContributorEntity;
import com.gitdetective.entity.FileEntity;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.entity.MethodEntity;
import com.gitdetective.entity.PackageEntity;
import com.gitdetective.entity.TagEntity;
import com.gitdetective.exception.ResourceNotFoundException;
import com.gitdetective.repository.BranchJpaRepository;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.ContributorJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import com.gitdetective.repository.MethodJpaRepository;
import com.gitdetective.repository.PackageJpaRepository;
import com.gitdetective.repository.TagJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestigationTargetResolver {

    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final MethodJpaRepository methodJpaRepository;
    private final PackageJpaRepository packageJpaRepository;
    private final FileJpaRepository fileJpaRepository;
    private final CommitJpaRepository commitJpaRepository;
    private final ContributorJpaRepository contributorJpaRepository;
    private final BranchJpaRepository branchJpaRepository;
    private final TagJpaRepository tagJpaRepository;

    public InvestigationTarget resolve(
            UUID repositoryId, InvestigationTargetType type, String targetRef) {
        return switch (type) {
            case CLASS -> resolveClass(repositoryId, targetRef);
            case METHOD -> resolveMethod(repositoryId, targetRef);
            case PACKAGE -> resolvePackage(repositoryId, targetRef);
            case FILE -> resolveFile(repositoryId, targetRef);
            case COMMIT -> resolveCommit(repositoryId, targetRef);
            case CONTRIBUTOR -> resolveContributor(repositoryId, targetRef);
            case BRANCH -> resolveBranch(repositoryId, targetRef);
            case TAG -> resolveTag(repositoryId, targetRef);
        };
    }

    private InvestigationTarget resolveClass(UUID repositoryId, String ref) {
        CodeTypeEntity type =
                findUuid(ref)
                        .flatMap(
                                id ->
                                        codeTypeJpaRepository.findByIdAndRepositoryId(
                                                id, repositoryId))
                        .or(
                                () ->
                                        codeTypeJpaRepository
                                                .findByRepositoryIdAndFullyQualifiedName(
                                                        repositoryId, ref))
                        .orElseThrow(() -> notFound("Class", ref));
        FileEntity file =
                type.getFileId() == null
                        ? null
                        : fileJpaRepository.findById(type.getFileId()).orElse(null);
        PackageEntity pkg =
                type.getPackageId() == null
                        ? null
                        : packageJpaRepository.findById(type.getPackageId()).orElse(null);
        return new InvestigationTarget(
                InvestigationTargetType.CLASS,
                type.getId().toString(),
                type.getFullyQualifiedName(),
                repositoryId,
                file == null ? null : file.getId(),
                file == null ? null : file.getPath(),
                pkg == null ? null : pkg.getId(),
                pkg == null
                        ? type.getFullyQualifiedName().contains(".")
                                ? type.getFullyQualifiedName()
                                        .substring(0, type.getFullyQualifiedName().lastIndexOf('.'))
                                : ""
                        : pkg.getName(),
                type.getId(),
                type.getFullyQualifiedName(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InvestigationTarget resolveMethod(UUID repositoryId, String ref) {
        MethodEntity method =
                findUuid(ref)
                        .flatMap(methodJpaRepository::findById)
                        .orElseThrow(() -> notFound("Method", ref));
        CodeTypeEntity type =
                codeTypeJpaRepository
                        .findByIdAndRepositoryId(method.getTypeId(), repositoryId)
                        .orElseThrow(() -> notFound("Method type", ref));
        InvestigationTarget classTarget = resolveClass(repositoryId, type.getId().toString());
        return new InvestigationTarget(
                InvestigationTargetType.METHOD,
                method.getId().toString(),
                type.getFullyQualifiedName() + "#" + method.getName(),
                repositoryId,
                classTarget.fileId(),
                classTarget.filePath(),
                classTarget.packageId(),
                classTarget.packageName(),
                type.getId(),
                type.getFullyQualifiedName(),
                method.getId(),
                method.getSignature(),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InvestigationTarget resolvePackage(UUID repositoryId, String ref) {
        PackageEntity pkg =
                findUuid(ref)
                        .flatMap(
                                id ->
                                        packageJpaRepository.findByIdAndRepositoryId(
                                                id, repositoryId))
                        .or(() -> packageJpaRepository.findByRepositoryIdAndName(repositoryId, ref))
                        .orElseThrow(() -> notFound("Package", ref));
        return new InvestigationTarget(
                InvestigationTargetType.PACKAGE,
                pkg.getId().toString(),
                pkg.getName(),
                repositoryId,
                null,
                pkg.getPath(),
                pkg.getId(),
                pkg.getName(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InvestigationTarget resolveFile(UUID repositoryId, String ref) {
        FileEntity file =
                findUuid(ref)
                        .flatMap(id -> fileJpaRepository.findByIdAndRepositoryId(id, repositoryId))
                        .or(() -> fileJpaRepository.findByRepositoryIdAndPath(repositoryId, ref))
                        .orElseThrow(() -> notFound("File", ref));
        return new InvestigationTarget(
                InvestigationTargetType.FILE,
                file.getId().toString(),
                file.getPath(),
                repositoryId,
                file.getId(),
                file.getPath(),
                null,
                file.getPackageName(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InvestigationTarget resolveCommit(UUID repositoryId, String ref) {
        CommitEntity commit =
                findUuid(ref)
                        .flatMap(
                                id -> commitJpaRepository.findByIdAndRepositoryId(id, repositoryId))
                        .or(() -> commitJpaRepository.findByRepositoryIdAndSha(repositoryId, ref))
                        .orElseThrow(() -> notFound("Commit", ref));
        return new InvestigationTarget(
                InvestigationTargetType.COMMIT,
                commit.getId().toString(),
                commit.getSha(),
                repositoryId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                commit.getId(),
                commit.getSha(),
                null,
                commit.getAuthorEmail(),
                null,
                null);
    }

    private InvestigationTarget resolveContributor(UUID repositoryId, String ref) {
        ContributorEntity contributor =
                findUuid(ref)
                        .flatMap(
                                id ->
                                        contributorJpaRepository.findByIdAndRepositoryId(
                                                id, repositoryId))
                        .or(
                                () ->
                                        contributorJpaRepository
                                                .findByRepositoryIdAndEmailIgnoreCase(
                                                        repositoryId, ref))
                        .orElseThrow(() -> notFound("Contributor", ref));
        return new InvestigationTarget(
                InvestigationTargetType.CONTRIBUTOR,
                contributor.getId().toString(),
                contributor.getName() + " <" + contributor.getEmail() + ">",
                repositoryId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                contributor.getId(),
                contributor.getEmail(),
                null,
                null);
    }

    private InvestigationTarget resolveBranch(UUID repositoryId, String ref) {
        BranchEntity branch =
                findUuid(ref)
                        .flatMap(
                                id -> branchJpaRepository.findByIdAndRepositoryId(id, repositoryId))
                        .or(() -> branchJpaRepository.findByRepositoryIdAndName(repositoryId, ref))
                        .orElseThrow(() -> notFound("Branch", ref));
        return new InvestigationTarget(
                InvestigationTargetType.BRANCH,
                branch.getId().toString(),
                branch.getName(),
                repositoryId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                branch.getHeadCommitSha(),
                null,
                null,
                branch.getName(),
                null);
    }

    private InvestigationTarget resolveTag(UUID repositoryId, String ref) {
        TagEntity tag =
                findUuid(ref)
                        .flatMap(id -> tagJpaRepository.findByIdAndRepositoryId(id, repositoryId))
                        .or(() -> tagJpaRepository.findByRepositoryIdAndName(repositoryId, ref))
                        .orElseThrow(() -> notFound("Tag", ref));
        return new InvestigationTarget(
                InvestigationTargetType.TAG,
                tag.getId().toString(),
                tag.getName(),
                repositoryId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                tag.getCommitSha(),
                null,
                null,
                null,
                tag.getName());
    }

    private Optional<UUID> findUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private ResourceNotFoundException notFound(String kind, String ref) {
        return new ResourceNotFoundException(kind + " not found for investigation target: " + ref);
    }
}
