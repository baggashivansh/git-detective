package com.gitdetective.analyzer;

import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.AnnotationEntity;
import com.gitdetective.entity.BranchEntity;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.CodeTypeKind;
import com.gitdetective.entity.CommitBranchEntity;
import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.CommitFileChangeEntity;
import com.gitdetective.entity.CommitParentEntity;
import com.gitdetective.entity.CommitTagEntity;
import com.gitdetective.entity.ContributorEntity;
import com.gitdetective.entity.FieldEntity;
import com.gitdetective.entity.FileChangeType;
import com.gitdetective.entity.FileEntity;
import com.gitdetective.entity.FileExportEntity;
import com.gitdetective.entity.FileImportEntity;
import com.gitdetective.entity.LanguageStatisticEntity;
import com.gitdetective.entity.MethodEntity;
import com.gitdetective.entity.PackageEntity;
import com.gitdetective.entity.RepositoryStatisticsEntity;
import com.gitdetective.entity.TagEntity;
import com.gitdetective.entity.TypeInterfaceEntity;
import com.gitdetective.git.GitRepositorySnapshot;
import com.gitdetective.graph.DependencyGraphBuilder;
import com.gitdetective.indexer.IndexedFile;
import com.gitdetective.parser.JavaSourceParser;
import com.gitdetective.parser.ParsedJavaFile;
import com.gitdetective.repository.AnnotationJpaRepository;
import com.gitdetective.repository.BranchJpaRepository;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.CommitBranchJpaRepository;
import com.gitdetective.repository.CommitFileChangeJpaRepository;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.CommitParentJpaRepository;
import com.gitdetective.repository.CommitTagJpaRepository;
import com.gitdetective.repository.ContributorJpaRepository;
import com.gitdetective.repository.FieldJpaRepository;
import com.gitdetective.repository.FileExportJpaRepository;
import com.gitdetective.repository.FileImportJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import com.gitdetective.repository.LanguageStatisticJpaRepository;
import com.gitdetective.repository.MethodJpaRepository;
import com.gitdetective.repository.PackageJpaRepository;
import com.gitdetective.repository.RepositoryStatisticsJpaRepository;
import com.gitdetective.repository.TagJpaRepository;
import com.gitdetective.repository.TypeInterfaceJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RepositoryAnalysisPersistenceService {

    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final BranchJpaRepository branchJpaRepository;
    private final TagJpaRepository tagJpaRepository;
    private final CommitJpaRepository commitJpaRepository;
    private final CommitParentJpaRepository commitParentJpaRepository;
    private final CommitBranchJpaRepository commitBranchJpaRepository;
    private final CommitTagJpaRepository commitTagJpaRepository;
    private final CommitFileChangeJpaRepository commitFileChangeJpaRepository;
    private final ContributorJpaRepository contributorJpaRepository;
    private final FileJpaRepository fileJpaRepository;
    private final PackageJpaRepository packageJpaRepository;
    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final MethodJpaRepository methodJpaRepository;
    private final FieldJpaRepository fieldJpaRepository;
    private final TypeInterfaceJpaRepository typeInterfaceJpaRepository;
    private final AnnotationJpaRepository annotationJpaRepository;
    private final FileImportJpaRepository fileImportJpaRepository;
    private final FileExportJpaRepository fileExportJpaRepository;
    private final LanguageStatisticJpaRepository languageStatisticJpaRepository;
    private final RepositoryStatisticsJpaRepository repositoryStatisticsJpaRepository;
    private final JavaSourceParser javaSourceParser;
    private final DependencyGraphBuilder dependencyGraphBuilder;

    @Transactional
    public void updateStatus(
            UUID repositoryId, AnalysisStatus status, String message, int progressPercent) {
        CodeRepository repository =
                codeRepositoryJpaRepository.findById(repositoryId).orElseThrow();
        repository.setStatus(status);
        repository.setStatusMessage(message);
        repository.setProgressPercent(progressPercent);
        if (status == AnalysisStatus.FAILED) {
            repository.setErrorMessage(message);
        }
        codeRepositoryJpaRepository.save(repository);
    }

    @Transactional
    public void persistGitSnapshot(UUID repositoryId, GitRepositorySnapshot snapshot) {
        CodeRepository repository =
                codeRepositoryJpaRepository.findById(repositoryId).orElseThrow();
        repository.setName(snapshot.name());
        repository.setDefaultBranch(snapshot.defaultBranch());
        repository.setRemoteUrl(snapshot.remoteUrl());
        repository.setTotalCommits(snapshot.totalCommits());
        repository.setSizeBytes(snapshot.sizeBytes());
        repository.setLatestCommitSha(snapshot.latestCommitSha());
        repository.setPrimaryLanguage(snapshot.detectedPrimaryLanguage());
        codeRepositoryJpaRepository.save(repository);

        for (GitRepositorySnapshot.BranchInfo branch : snapshot.branches()) {
            branchJpaRepository.save(
                    BranchEntity.builder()
                            .repositoryId(repositoryId)
                            .name(branch.name())
                            .defaultBranch(branch.defaultBranch())
                            .headCommitSha(branch.headCommitSha())
                            .build());
        }
        for (GitRepositorySnapshot.TagInfo tag : snapshot.tags()) {
            tagJpaRepository.save(
                    TagEntity.builder()
                            .repositoryId(repositoryId)
                            .name(tag.name())
                            .commitSha(tag.commitSha())
                            .build());
        }

        Map<String, Long> contributorCommits = new HashMap<>();
        Map<String, String> contributorNames = new HashMap<>();
        Map<String, Long> contributorAdditions = new HashMap<>();
        Map<String, Long> contributorDeletions = new HashMap<>();
        Map<String, Set<String>> contributorFiles = new HashMap<>();
        Map<String, java.time.Instant> contributorLast = new HashMap<>();

        for (GitRepositorySnapshot.CommitInfo commit : snapshot.commits()) {
            CommitEntity saved =
                    commitJpaRepository.save(
                            CommitEntity.builder()
                                    .repositoryId(repositoryId)
                                    .sha(commit.sha())
                                    .authorName(commit.authorName())
                                    .authorEmail(commit.authorEmail())
                                    .authoredAt(commit.authoredAt())
                                    .message(commit.message())
                                    .merge(commit.merge())
                                    .insertions(commit.insertions())
                                    .deletions(commit.deletions())
                                    .filesChangedCount(commit.fileChanges().size())
                                    .build());

            int order = 0;
            for (String parent : commit.parentShas()) {
                commitParentJpaRepository.save(
                        CommitParentEntity.builder()
                                .commitId(saved.getId())
                                .parentSha(parent)
                                .parentOrder(order++)
                                .build());
            }
            for (String branch : commit.branchNames()) {
                commitBranchJpaRepository.save(
                        CommitBranchEntity.builder()
                                .commitId(saved.getId())
                                .branchName(branch)
                                .build());
            }
            for (String tag : commit.tagNames()) {
                commitTagJpaRepository.save(
                        CommitTagEntity.builder().commitId(saved.getId()).tagName(tag).build());
            }
            for (GitRepositorySnapshot.FileChangeInfo change : commit.fileChanges()) {
                commitFileChangeJpaRepository.save(
                        CommitFileChangeEntity.builder()
                                .commitId(saved.getId())
                                .path(change.path())
                                .changeType(mapChangeType(change.changeType()))
                                .insertions(change.insertions())
                                .deletions(change.deletions())
                                .build());
                contributorFiles
                        .computeIfAbsent(commit.authorEmail(), ignored -> new HashSet<>())
                        .add(change.path());
            }

            String email = commit.authorEmail().toLowerCase(Locale.ROOT);
            contributorNames.put(email, commit.authorName());
            contributorCommits.merge(email, 1L, Long::sum);
            contributorAdditions.merge(email, (long) commit.insertions(), Long::sum);
            contributorDeletions.merge(email, (long) commit.deletions(), Long::sum);
            contributorLast.merge(
                    email,
                    commit.authoredAt(),
                    (left, right) -> left.isAfter(right) ? left : right);
        }

        long totalCommits = Math.max(snapshot.totalCommits(), 1);
        for (Map.Entry<String, Long> entry : contributorCommits.entrySet()) {
            String email = entry.getKey();
            BigDecimal percentage =
                    BigDecimal.valueOf(entry.getValue() * 100.0 / totalCommits)
                            .setScale(3, RoundingMode.HALF_UP);
            contributorJpaRepository.save(
                    ContributorEntity.builder()
                            .repositoryId(repositoryId)
                            .name(contributorNames.get(email))
                            .email(email)
                            .commitCount(entry.getValue())
                            .filesModified(contributorFiles.getOrDefault(email, Set.of()).size())
                            .linesAdded(contributorAdditions.getOrDefault(email, 0L))
                            .linesDeleted(contributorDeletions.getOrDefault(email, 0L))
                            .lastContributionAt(contributorLast.get(email))
                            .contributionPercentage(percentage)
                            .build());
        }
    }

    @Transactional
    public void persistIndexedFiles(UUID repositoryId, List<IndexedFile> indexedFiles) {
        Map<String, Long> languageFiles = new HashMap<>();
        Map<String, Long> languageLines = new HashMap<>();
        Map<String, Long> languageBytes = new HashMap<>();
        Map<String, PackageEntity> packages = new HashMap<>();

        long totalFiles = 0;
        long totalDirs = 0;
        long totalLines = 0;
        long binaryCount = 0;
        long ignoredCount = 0;
        long classCount = 0;
        long interfaceCount = 0;
        long enumCount = 0;
        long methodCountTotal = 0;

        for (IndexedFile indexed : indexedFiles) {
            if (indexed.directory()) {
                totalDirs++;
            } else {
                totalFiles++;
                totalLines += indexed.lineCount();
                if (indexed.binary()) {
                    binaryCount++;
                }
            }
            if (indexed.ignored()) {
                ignoredCount++;
            }

            FileEntity fileEntity =
                    fileJpaRepository.save(
                            FileEntity.builder()
                                    .repositoryId(repositoryId)
                                    .path(indexed.relativePath())
                                    .name(indexed.name())
                                    .parentPath(indexed.parentPath())
                                    .extension(indexed.extension())
                                    .language(indexed.language())
                                    .sizeBytes(indexed.sizeBytes())
                                    .lineCount(indexed.lineCount())
                                    .directory(indexed.directory())
                                    .binary(indexed.binary())
                                    .hidden(indexed.hidden())
                                    .ignored(indexed.ignored())
                                    .contentHash(indexed.contentHash())
                                    .createdAtFs(indexed.createdAtFs())
                                    .modifiedAtFs(indexed.modifiedAtFs())
                                    .build());

            if (!indexed.directory()
                    && !indexed.binary()
                    && "Java".equals(indexed.language())
                    && indexed.relativePath().endsWith(".java")) {
                ParsedJavaFile parsed = javaSourceParser.parse(indexed.absolutePath());
                fileEntity.setPackageName(parsed.packageName());
                fileEntity.setImportCount(parsed.imports().size());
                fileEntity.setExportCount(parsed.exports().size());

                int methodCount = 0;
                int fieldCount = 0;

                if (parsed.packageName() != null && !parsed.packageName().isBlank()) {
                    packages.computeIfAbsent(
                            parsed.packageName(),
                            name ->
                                    packageJpaRepository.save(
                                            PackageEntity.builder()
                                                    .repositoryId(repositoryId)
                                                    .name(name)
                                                    .path(indexed.parentPath())
                                                    .fileCount(0)
                                                    .build()));
                    PackageEntity packageEntity = packages.get(parsed.packageName());
                    packageEntity.setFileCount(packageEntity.getFileCount() + 1);
                }

                for (ParsedJavaFile.ImportInfo importInfo : parsed.imports()) {
                    fileImportJpaRepository.save(
                            FileImportEntity.builder()
                                    .fileId(fileEntity.getId())
                                    .importName(importInfo.name())
                                    .staticImport(importInfo.staticImport())
                                    .asterisk(importInfo.asterisk())
                                    .build());
                }
                for (String export : parsed.exports()) {
                    fileExportJpaRepository.save(
                            FileExportEntity.builder()
                                    .fileId(fileEntity.getId())
                                    .exportName(export)
                                    .build());
                }

                PackageEntity packageEntity =
                        parsed.packageName() == null || parsed.packageName().isBlank()
                                ? null
                                : packages.get(parsed.packageName());

                for (ParsedJavaFile.TypeInfo type : parsed.types()) {
                    CodeTypeKind kind = CodeTypeKind.valueOf(type.kind());
                    if (kind == CodeTypeKind.CLASS) {
                        classCount++;
                    } else if (kind == CodeTypeKind.INTERFACE) {
                        interfaceCount++;
                    } else if (kind == CodeTypeKind.ENUM) {
                        enumCount++;
                    }
                    CodeTypeEntity typeEntity =
                            codeTypeJpaRepository.save(
                                    CodeTypeEntity.builder()
                                            .repositoryId(repositoryId)
                                            .packageId(
                                                    packageEntity == null
                                                            ? null
                                                            : packageEntity.getId())
                                            .fileId(fileEntity.getId())
                                            .name(type.name())
                                            .fullyQualifiedName(type.fullyQualifiedName())
                                            .kind(kind)
                                            .visibility(type.visibility())
                                            .superclassName(type.superclassName())
                                            .startLine(type.startLine())
                                            .endLine(type.endLine())
                                            .build());

                    for (String iface : type.implementedInterfaces()) {
                        typeInterfaceJpaRepository.save(
                                TypeInterfaceEntity.builder()
                                        .typeId(typeEntity.getId())
                                        .interfaceName(iface)
                                        .build());
                    }
                    for (String annotation : type.annotations()) {
                        annotationJpaRepository.save(
                                AnnotationEntity.builder()
                                        .ownerKind("TYPE")
                                        .ownerId(typeEntity.getId())
                                        .name(annotation)
                                        .build());
                    }
                    for (ParsedJavaFile.MethodInfo method : type.methods()) {
                        methodCount++;
                        methodCountTotal++;
                        MethodEntity methodEntity =
                                methodJpaRepository.save(
                                        MethodEntity.builder()
                                                .typeId(typeEntity.getId())
                                                .name(method.name())
                                                .signature(method.signature())
                                                .returnType(method.returnType())
                                                .visibility(method.visibility())
                                                .constructor(method.constructor())
                                                .parameterCount(method.parameterCount())
                                                .startLine(method.startLine())
                                                .build());
                        for (String annotation : method.annotations()) {
                            annotationJpaRepository.save(
                                    AnnotationEntity.builder()
                                            .ownerKind("METHOD")
                                            .ownerId(methodEntity.getId())
                                            .name(annotation)
                                            .build());
                        }
                    }
                    for (ParsedJavaFile.FieldInfo field : type.fields()) {
                        fieldCount++;
                        FieldEntity fieldEntity =
                                fieldJpaRepository.save(
                                        FieldEntity.builder()
                                                .typeId(typeEntity.getId())
                                                .name(field.name())
                                                .typeName(field.typeName())
                                                .visibility(field.visibility())
                                                .build());
                        for (String annotation : field.annotations()) {
                            annotationJpaRepository.save(
                                    AnnotationEntity.builder()
                                            .ownerKind("FIELD")
                                            .ownerId(fieldEntity.getId())
                                            .name(annotation)
                                            .build());
                        }
                    }
                }

                fileEntity.setMethodCount(methodCount);
                fileEntity.setFieldCount(fieldCount);
                fileJpaRepository.save(fileEntity);
                dependencyGraphBuilder.buildForJavaFile(
                        repositoryId, indexed.relativePath(), parsed.packageName(), parsed);
            }

            if (!indexed.directory() && indexed.language() != null) {
                languageFiles.merge(indexed.language(), 1L, Long::sum);
                languageLines.merge(indexed.language(), (long) indexed.lineCount(), Long::sum);
                languageBytes.merge(indexed.language(), indexed.sizeBytes(), Long::sum);
            }
        }

        packageJpaRepository.saveAll(packages.values());

        long totalLanguageFiles = languageFiles.values().stream().mapToLong(Long::longValue).sum();
        for (Map.Entry<String, Long> entry : languageFiles.entrySet()) {
            BigDecimal percentage =
                    totalLanguageFiles == 0
                            ? BigDecimal.ZERO
                            : BigDecimal.valueOf(entry.getValue() * 100.0 / totalLanguageFiles)
                                    .setScale(3, RoundingMode.HALF_UP);
            languageStatisticJpaRepository.save(
                    LanguageStatisticEntity.builder()
                            .repositoryId(repositoryId)
                            .language(entry.getKey())
                            .fileCount(entry.getValue().intValue())
                            .lineCount(languageLines.getOrDefault(entry.getKey(), 0L))
                            .byteCount(languageBytes.getOrDefault(entry.getKey(), 0L))
                            .percentage(percentage)
                            .build());
        }

        long contributors =
                contributorJpaRepository
                        .findByRepositoryIdOrderByCommitCountDesc(repositoryId)
                        .size();
        long branches = branchJpaRepository.findByRepositoryId(repositoryId).size();
        long tags = tagJpaRepository.findByRepositoryId(repositoryId).size();

        repositoryStatisticsJpaRepository.save(
                RepositoryStatisticsEntity.builder()
                        .repositoryId(repositoryId)
                        .totalFiles(totalFiles)
                        .totalDirectories(totalDirs)
                        .totalLines(totalLines)
                        .totalPackages(packages.size())
                        .totalClasses(classCount)
                        .totalInterfaces(interfaceCount)
                        .totalEnums(enumCount)
                        .totalMethods(methodCountTotal)
                        .totalContributors(contributors)
                        .totalBranches(branches)
                        .totalTags(tags)
                        .binaryFileCount(binaryCount)
                        .ignoredFileCount(ignoredCount)
                        .build());
    }

    private FileChangeType mapChangeType(String changeType) {
        try {
            return FileChangeType.valueOf(changeType.toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            return FileChangeType.MODIFY;
        }
    }
}
