package com.gitdetective.investigation;

import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.DependencyEdgeEntity;
import com.gitdetective.entity.FileEntity;
import com.gitdetective.entity.MethodEntity;
import com.gitdetective.entity.PackageEntity;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.CommitFileChangeJpaRepository;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.ContributorJpaRepository;
import com.gitdetective.repository.DependencyEdgeJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import com.gitdetective.repository.MethodJpaRepository;
import com.gitdetective.repository.PackageJpaRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HotspotDetector {

    private final FileJpaRepository fileJpaRepository;
    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final MethodJpaRepository methodJpaRepository;
    private final PackageJpaRepository packageJpaRepository;
    private final ContributorJpaRepository contributorJpaRepository;
    private final CommitJpaRepository commitJpaRepository;
    private final CommitFileChangeJpaRepository commitFileChangeJpaRepository;
    private final DependencyEdgeJpaRepository dependencyEdgeJpaRepository;

    public List<Hotspot> detect(UUID repositoryId) {
        List<Hotspot> hotspots = new ArrayList<>();

        Map<String, Long> fileChangeCounts = new HashMap<>();
        List<CommitEntity> commits =
                commitJpaRepository
                        .findByRepositoryIdOrderByAuthoredAtDesc(
                                repositoryId, PageRequest.of(0, 2000))
                        .getContent();
        for (CommitEntity commit : commits) {
            commitFileChangeJpaRepository
                    .findByCommitId(commit.getId())
                    .forEach(change -> fileChangeCounts.merge(change.getPath(), 1L, Long::sum));
        }
        fileChangeCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(
                        entry ->
                                hotspots.add(
                                        new Hotspot(
                                                "FREQUENTLY_MODIFIED_FILE",
                                                entry.getKey(),
                                                entry.getKey(),
                                                BigDecimal.valueOf(entry.getValue()),
                                                "Modification count=" + entry.getValue())));

        fileJpaRepository.findByRepositoryIdAndDirectoryFalse(repositoryId).stream()
                .sorted(Comparator.comparingInt(FileEntity::getLineCount).reversed())
                .limit(10)
                .forEach(
                        file ->
                                hotspots.add(
                                        new Hotspot(
                                                "LARGE_FILE",
                                                file.getId().toString(),
                                                file.getPath(),
                                                BigDecimal.valueOf(file.getLineCount()),
                                                "LOC=" + file.getLineCount())));

        for (CodeTypeEntity type :
                codeTypeJpaRepository.findByRepositoryIdOrderByFullyQualifiedNameAsc(
                        repositoryId)) {
            List<MethodEntity> methods = methodJpaRepository.findByTypeId(type.getId());
            if (methods.size() >= 20) {
                hotspots.add(
                        new Hotspot(
                                "LARGE_CLASS",
                                type.getId().toString(),
                                type.getFullyQualifiedName(),
                                BigDecimal.valueOf(methods.size()),
                                "Method count=" + methods.size()));
            }
            methods.stream()
                    .filter(method -> method.getParameterCount() >= 6)
                    .limit(5)
                    .forEach(
                            method ->
                                    hotspots.add(
                                            new Hotspot(
                                                    "LARGE_METHOD",
                                                    method.getId().toString(),
                                                    type.getName() + "#" + method.getName(),
                                                    BigDecimal.valueOf(method.getParameterCount()),
                                                    "Parameter count="
                                                            + method.getParameterCount())));
        }

        Map<UUID, Long> dependencyDegree = new HashMap<>();
        for (DependencyEdgeEntity edge :
                dependencyEdgeJpaRepository.findByRepositoryId(repositoryId)) {
            dependencyDegree.merge(edge.getSourceNodeId(), 1L, Long::sum);
            dependencyDegree.merge(edge.getTargetNodeId(), 1L, Long::sum);
        }
        dependencyDegree.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(
                        entry ->
                                hotspots.add(
                                        new Hotspot(
                                                "HIGH_DEPENDENCY_NODE",
                                                entry.getKey().toString(),
                                                entry.getKey().toString(),
                                                BigDecimal.valueOf(entry.getValue()),
                                                "Degree=" + entry.getValue())));

        packageJpaRepository.findByRepositoryIdOrderByNameAsc(repositoryId).stream()
                .sorted(Comparator.comparingInt(PackageEntity::getFileCount).reversed())
                .limit(10)
                .forEach(
                        pkg ->
                                hotspots.add(
                                        new Hotspot(
                                                "ACTIVE_PACKAGE",
                                                pkg.getId().toString(),
                                                pkg.getName(),
                                                BigDecimal.valueOf(pkg.getFileCount()),
                                                "File count=" + pkg.getFileCount())));

        contributorJpaRepository.findByRepositoryIdOrderByCommitCountDesc(repositoryId).stream()
                .limit(10)
                .forEach(
                        contributor ->
                                hotspots.add(
                                        new Hotspot(
                                                "ACTIVE_CONTRIBUTOR",
                                                contributor.getId().toString(),
                                                contributor.getName(),
                                                BigDecimal.valueOf(contributor.getCommitCount()),
                                                "Commits=" + contributor.getCommitCount())));

        List<Hotspot> ranked = new ArrayList<>();
        int rank = 1;
        for (Hotspot hotspot : hotspots) {
            ranked.add(
                    new Hotspot(
                            hotspot.kind(),
                            hotspot.itemRef(),
                            hotspot.itemLabel(),
                            hotspot.score(),
                            hotspot.detail(),
                            rank++));
        }
        return ranked;
    }

    public record Hotspot(
            String kind,
            String itemRef,
            String itemLabel,
            BigDecimal score,
            String detail,
            int rank) {
        Hotspot(String kind, String itemRef, String itemLabel, BigDecimal score, String detail) {
            this(kind, itemRef, itemLabel, score, detail, 0);
        }
    }
}
