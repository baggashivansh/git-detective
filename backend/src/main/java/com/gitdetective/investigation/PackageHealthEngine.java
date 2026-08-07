package com.gitdetective.investigation;

import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.CommitEntity;
import com.gitdetective.entity.PackageEntity;
import com.gitdetective.entity.RiskLevel;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.CommitFileChangeJpaRepository;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.DependencyEdgeJpaRepository;
import com.gitdetective.repository.DependencyNodeJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import com.gitdetective.repository.MethodJpaRepository;
import com.gitdetective.repository.PackageJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PackageHealthEngine {

    private final PackageJpaRepository packageJpaRepository;
    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final MethodJpaRepository methodJpaRepository;
    private final FileJpaRepository fileJpaRepository;
    private final CommitJpaRepository commitJpaRepository;
    private final CommitFileChangeJpaRepository commitFileChangeJpaRepository;
    private final DependencyNodeJpaRepository dependencyNodeJpaRepository;
    private final DependencyEdgeJpaRepository dependencyEdgeJpaRepository;

    public List<PackageHealth> calculate(UUID repositoryId) {
        Map<String, Long> packageMods = new HashMap<>();
        Map<String, Set<String>> packageAuthors = new HashMap<>();

        List<CommitEntity> commits =
                commitJpaRepository
                        .findByRepositoryIdOrderByAuthoredAtDesc(
                                repositoryId, PageRequest.of(0, 2000))
                        .getContent();
        Map<String, String> pathToPackage = new HashMap<>();
        fileJpaRepository
                .findByRepositoryIdAndDirectoryFalse(repositoryId)
                .forEach(
                        file -> {
                            if (file.getPackageName() != null) {
                                pathToPackage.put(file.getPath(), file.getPackageName());
                            }
                        });

        for (CommitEntity commit : commits) {
            commitFileChangeJpaRepository
                    .findByCommitId(commit.getId())
                    .forEach(
                            change -> {
                                String pkg = pathToPackage.get(change.getPath());
                                if (pkg == null) {
                                    return;
                                }
                                packageMods.merge(pkg, 1L, Long::sum);
                                packageAuthors
                                        .computeIfAbsent(pkg, ignored -> new HashSet<>())
                                        .add(commit.getAuthorEmail());
                            });
        }

        Map<String, Long> packageDeps = new HashMap<>();
        dependencyNodeJpaRepository.findByRepositoryId(repositoryId).stream()
                .filter(node -> node.getNodeKey().startsWith("package:"))
                .forEach(
                        node -> {
                            long degree =
                                    dependencyEdgeJpaRepository
                                                    .findByRepositoryIdAndSourceNodeId(
                                                            repositoryId, node.getId())
                                                    .size()
                                            + dependencyEdgeJpaRepository
                                                    .findByRepositoryIdAndTargetNodeId(
                                                            repositoryId, node.getId())
                                                    .size();
                            packageDeps.put(node.getLabel(), degree);
                        });

        List<PackageHealth> results = new ArrayList<>();
        for (PackageEntity pkg :
                packageJpaRepository.findByRepositoryIdOrderByNameAsc(repositoryId)) {
            List<CodeTypeEntity> types =
                    codeTypeJpaRepository
                            .findByRepositoryIdOrderByFullyQualifiedNameAsc(repositoryId)
                            .stream()
                            .filter(type -> pkg.getId().equals(type.getPackageId()))
                            .toList();
            int methodCount =
                    types.stream()
                            .mapToInt(type -> methodJpaRepository.findByTypeId(type.getId()).size())
                            .sum();
            int size = pkg.getFileCount();
            long deps = packageDeps.getOrDefault(pkg.getName(), 0L);
            long mods = packageMods.getOrDefault(pkg.getName(), 0L);
            int contributors = packageAuthors.getOrDefault(pkg.getName(), Set.of()).size();

            BigDecimal complexity =
                    BigDecimal.valueOf(size * 1.0 + methodCount * 0.5 + types.size() * 2.0)
                            .setScale(3, RoundingMode.HALF_UP);
            BigDecimal modificationFrequency =
                    BigDecimal.valueOf(mods).setScale(3, RoundingMode.HALF_UP);
            BigDecimal growth = BigDecimal.valueOf(size + mods).setScale(3, RoundingMode.HALF_UP);

            RiskLevel risk;
            double riskScore = complexity.doubleValue() + deps * 2 + mods * 0.5;
            if (riskScore >= 80) {
                risk = RiskLevel.HIGH;
            } else if (riskScore >= 35) {
                risk = RiskLevel.MEDIUM;
            } else {
                risk = RiskLevel.LOW;
            }

            results.add(
                    new PackageHealth(
                            pkg.getName(),
                            complexity,
                            (int) deps,
                            size,
                            modificationFrequency,
                            contributors,
                            growth,
                            risk));
        }
        return results;
    }

    public record PackageHealth(
            String packageName,
            BigDecimal complexityScore,
            int dependencyCount,
            int packageSize,
            BigDecimal modificationFrequency,
            int contributorCount,
            BigDecimal growthScore,
            RiskLevel riskLevel) {}
}
