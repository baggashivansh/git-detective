package com.gitdetective.analyzer;

import com.gitdetective.dto.response.CodeTypeResponse;
import com.gitdetective.dto.response.CommitResponse;
import com.gitdetective.dto.response.ContributorResponse;
import com.gitdetective.dto.response.LanguageStatisticResponse;
import com.gitdetective.dto.response.PackageResponse;
import com.gitdetective.dto.response.RepositoryStatisticsResponse;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.dto.response.RepositoryTreeNodeResponse;
import com.gitdetective.dto.response.SearchResultResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.CommitBranchEntity;
import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.CommitParentEntity;
import com.gitdetective.entity.CommitTagEntity;
import com.gitdetective.entity.FileEntity;
import com.gitdetective.exception.ResourceNotFoundException;
import com.gitdetective.mapper.RepositoryResponseMapper;
import com.gitdetective.repository.BranchJpaRepository;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.CommitBranchJpaRepository;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.CommitParentJpaRepository;
import com.gitdetective.repository.CommitTagJpaRepository;
import com.gitdetective.repository.ContributorJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import com.gitdetective.repository.LanguageStatisticJpaRepository;
import com.gitdetective.repository.PackageJpaRepository;
import com.gitdetective.repository.RepositoryStatisticsJpaRepository;
import com.gitdetective.repository.TagJpaRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepositoryQueryService {

    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final FileJpaRepository fileJpaRepository;
    private final ContributorJpaRepository contributorJpaRepository;
    private final LanguageStatisticJpaRepository languageStatisticJpaRepository;
    private final CommitJpaRepository commitJpaRepository;
    private final CommitParentJpaRepository commitParentJpaRepository;
    private final CommitBranchJpaRepository commitBranchJpaRepository;
    private final CommitTagJpaRepository commitTagJpaRepository;
    private final PackageJpaRepository packageJpaRepository;
    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final RepositoryStatisticsJpaRepository repositoryStatisticsJpaRepository;
    private final BranchJpaRepository branchJpaRepository;
    private final TagJpaRepository tagJpaRepository;
    private final RepositoryResponseMapper mapper;

    public List<RepositorySummaryResponse> listRepositories() {
        return codeRepositoryJpaRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(mapper::toSummary)
                .toList();
    }

    public RepositorySummaryResponse getRepository(UUID id) {
        return mapper.toSummary(requireRepository(id));
    }

    public List<RepositoryTreeNodeResponse> getTree(UUID id) {
        requireRepository(id);
        List<FileEntity> files = fileJpaRepository.findByRepositoryIdOrderByPathAsc(id);
        Map<String, RepositoryTreeNodeResponse> nodes = new HashMap<>();
        List<RepositoryTreeNodeResponse> roots = new ArrayList<>();

        for (FileEntity file : files) {
            RepositoryTreeNodeResponse node =
                    new RepositoryTreeNodeResponse(
                            file.getId(),
                            file.getPath(),
                            file.getName(),
                            file.getParentPath(),
                            file.isDirectory(),
                            file.getLanguage(),
                            file.getExtension(),
                            file.getSizeBytes(),
                            new ArrayList<>());
            nodes.put(file.getPath(), node);
        }

        for (RepositoryTreeNodeResponse node : nodes.values()) {
            String parent = node.parentPath();
            if (parent == null || parent.isBlank() || !nodes.containsKey(parent)) {
                roots.add(node);
            } else {
                nodes.get(parent).children().add(node);
            }
        }

        sortTree(roots);
        return roots;
    }

    public List<ContributorResponse> getContributors(UUID id) {
        requireRepository(id);
        return contributorJpaRepository.findByRepositoryIdOrderByCommitCountDesc(id).stream()
                .map(mapper::toContributor)
                .toList();
    }

    public List<LanguageStatisticResponse> getLanguages(UUID id) {
        requireRepository(id);
        return languageStatisticJpaRepository.findByRepositoryIdOrderByPercentageDesc(id).stream()
                .map(mapper::toLanguage)
                .toList();
    }

    public List<CommitResponse> getCommits(UUID id, int page, int size) {
        requireRepository(id);
        return commitJpaRepository
                .findByRepositoryIdOrderByAuthoredAtDesc(id, PageRequest.of(page, size))
                .stream()
                .map(this::mapCommit)
                .toList();
    }

    public RepositoryStatisticsResponse getStatistics(UUID id) {
        CodeRepository repository = requireRepository(id);
        return repositoryStatisticsJpaRepository
                .findById(id)
                .map(stats -> mapper.toStatistics(stats, repository))
                .orElseGet(
                        () ->
                                new RepositoryStatisticsResponse(
                                        id,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        0,
                                        repository.getTotalCommits(),
                                        repository.getSizeBytes()));
    }

    public List<PackageResponse> getPackages(UUID id) {
        requireRepository(id);
        return packageJpaRepository.findByRepositoryIdOrderByNameAsc(id).stream()
                .map(mapper::toPackage)
                .toList();
    }

    public List<CodeTypeResponse> getClasses(UUID id) {
        requireRepository(id);
        Map<UUID, String> packageNames = new HashMap<>();
        packageJpaRepository
                .findByRepositoryIdOrderByNameAsc(id)
                .forEach(
                        packageEntity ->
                                packageNames.put(packageEntity.getId(), packageEntity.getName()));
        return codeTypeJpaRepository.findByRepositoryIdOrderByFullyQualifiedNameAsc(id).stream()
                .map(
                        type ->
                                mapper.toCodeType(
                                        type,
                                        type.getPackageId() == null
                                                ? null
                                                : packageNames.get(type.getPackageId())))
                .toList();
    }

    public SearchResultResponse search(UUID id, String query) {
        requireRepository(id);
        String q = query == null ? "" : query.trim();
        if (q.length() < 1) {
            return new SearchResultResponse(
                    q, List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }

        List<SearchResultResponse.SearchHit> files =
                fileJpaRepository.findByRepositoryIdAndDirectoryFalse(id).stream()
                        .filter(file -> contains(file.getPath(), q) || contains(file.getName(), q))
                        .limit(25)
                        .map(
                                file ->
                                        new SearchResultResponse.SearchHit(
                                                "file",
                                                file.getId().toString(),
                                                file.getName(),
                                                file.getPath()))
                        .toList();

        List<SearchResultResponse.SearchHit> folders =
                fileJpaRepository.findByRepositoryIdOrderByPathAsc(id).stream()
                        .filter(FileEntity::isDirectory)
                        .filter(file -> contains(file.getPath(), q) || contains(file.getName(), q))
                        .limit(25)
                        .map(
                                file ->
                                        new SearchResultResponse.SearchHit(
                                                "folder",
                                                file.getId().toString(),
                                                file.getName(),
                                                file.getPath()))
                        .toList();

        List<SearchResultResponse.SearchHit> classes =
                codeTypeJpaRepository.findByRepositoryIdAndNameContainingIgnoreCase(id, q).stream()
                        .limit(25)
                        .map(
                                type ->
                                        new SearchResultResponse.SearchHit(
                                                "class",
                                                type.getId().toString(),
                                                type.getName(),
                                                type.getFullyQualifiedName()))
                        .toList();

        List<SearchResultResponse.SearchHit> packages =
                packageJpaRepository.findByRepositoryIdOrderByNameAsc(id).stream()
                        .filter(packageEntity -> contains(packageEntity.getName(), q))
                        .limit(25)
                        .map(
                                packageEntity ->
                                        new SearchResultResponse.SearchHit(
                                                "package",
                                                packageEntity.getId().toString(),
                                                packageEntity.getName(),
                                                packageEntity.getPath()))
                        .toList();

        List<SearchResultResponse.SearchHit> commits =
                commitJpaRepository.findByRepositoryIdAndShaContainingIgnoreCase(id, q).stream()
                        .limit(15)
                        .map(this::toCommitHit)
                        .toList();
        if (commits.isEmpty()) {
            commits =
                    commitJpaRepository
                            .findByRepositoryIdAndMessageContainingIgnoreCase(id, q)
                            .stream()
                            .limit(15)
                            .map(this::toCommitHit)
                            .toList();
        }

        List<SearchResultResponse.SearchHit> branches =
                branchJpaRepository.findByRepositoryId(id).stream()
                        .filter(branch -> contains(branch.getName(), q))
                        .limit(25)
                        .map(
                                branch ->
                                        new SearchResultResponse.SearchHit(
                                                "branch",
                                                branch.getId().toString(),
                                                branch.getName(),
                                                branch.getHeadCommitSha()))
                        .toList();

        List<SearchResultResponse.SearchHit> tags =
                tagJpaRepository.findByRepositoryId(id).stream()
                        .filter(tag -> contains(tag.getName(), q))
                        .limit(25)
                        .map(
                                tag ->
                                        new SearchResultResponse.SearchHit(
                                                "tag",
                                                tag.getId().toString(),
                                                tag.getName(),
                                                tag.getCommitSha()))
                        .toList();

        return new SearchResultResponse(
                q, files, folders, classes, packages, commits, branches, tags);
    }

    private SearchResultResponse.SearchHit toCommitHit(CommitEntity commit) {
        return new SearchResultResponse.SearchHit(
                "commit",
                commit.getId().toString(),
                commit.getSha().substring(0, Math.min(7, commit.getSha().length())),
                commit.getMessage());
    }

    private CommitResponse mapCommit(CommitEntity commit) {
        List<String> parents =
                commitParentJpaRepository.findByCommitId(commit.getId()).stream()
                        .sorted(Comparator.comparingInt(CommitParentEntity::getParentOrder))
                        .map(CommitParentEntity::getParentSha)
                        .toList();
        List<String> branches =
                commitBranchJpaRepository.findByCommitId(commit.getId()).stream()
                        .map(CommitBranchEntity::getBranchName)
                        .toList();
        List<String> tags =
                commitTagJpaRepository.findByCommitId(commit.getId()).stream()
                        .map(CommitTagEntity::getTagName)
                        .toList();
        return mapper.toCommit(commit, parents, branches, tags);
    }

    private CodeRepository requireRepository(UUID id) {
        return codeRepositoryJpaRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repository not found: " + id));
    }

    private boolean contains(String value, String query) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private void sortTree(List<RepositoryTreeNodeResponse> nodes) {
        nodes.sort(
                Comparator.comparing(RepositoryTreeNodeResponse::directory)
                        .reversed()
                        .thenComparing(
                                RepositoryTreeNodeResponse::name, String.CASE_INSENSITIVE_ORDER));
        for (RepositoryTreeNodeResponse node : nodes) {
            sortTree(node.children());
        }
    }
}
