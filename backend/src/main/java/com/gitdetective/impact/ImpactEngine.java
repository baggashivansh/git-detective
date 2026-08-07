package com.gitdetective.impact;

import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.DependencyEdgeEntity;
import com.gitdetective.entity.DependencyNodeEntity;
import com.gitdetective.entity.DependencyNodeType;
import com.gitdetective.entity.FileEntity;
import com.gitdetective.entity.FileImportEntity;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.DependencyEdgeJpaRepository;
import com.gitdetective.repository.DependencyNodeJpaRepository;
import com.gitdetective.repository.FileImportJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Deterministic blast-radius estimation from indexed dependency edges and imports.
 *
 * <p>Score = min(100, affectedClasses * 2 + affectedPackages * 3 + maxDepth * 5).
 */
@Component
@RequiredArgsConstructor
public class ImpactEngine {

    private final DependencyNodeJpaRepository dependencyNodeJpaRepository;
    private final DependencyEdgeJpaRepository dependencyEdgeJpaRepository;
    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final FileJpaRepository fileJpaRepository;
    private final FileImportJpaRepository fileImportJpaRepository;

    public ImpactResult analyze(InvestigationTarget target) {
        List<DependencyNodeEntity> nodes =
                dependencyNodeJpaRepository.findByRepositoryId(target.repositoryId());
        List<DependencyEdgeEntity> edges =
                dependencyEdgeJpaRepository.findByRepositoryId(target.repositoryId());

        Map<UUID, DependencyNodeEntity> nodeById = new HashMap<>();
        Map<String, DependencyNodeEntity> nodeByKey = new HashMap<>();
        for (DependencyNodeEntity node : nodes) {
            nodeById.put(node.getId(), node);
            nodeByKey.put(node.getNodeKey(), node);
        }

        Map<UUID, List<DependencyEdgeEntity>> incoming = new HashMap<>();
        for (DependencyEdgeEntity edge : edges) {
            incoming.computeIfAbsent(edge.getTargetNodeId(), ignored -> new ArrayList<>())
                    .add(edge);
        }

        Set<String> seedKeys = seedKeys(target);
        Set<UUID> visited = new HashSet<>();
        Queue<DepthNode> queue = new ArrayDeque<>();
        List<ImpactItem> items = new ArrayList<>();
        List<String> evidence = new ArrayList<>();

        for (String key : seedKeys) {
            DependencyNodeEntity seed = nodeByKey.get(key);
            if (seed != null) {
                queue.add(new DepthNode(seed.getId(), 0));
                visited.add(seed.getId());
                evidence.add("Seed dependency node: " + key);
            }
        }

        // Also include files that import the target FQN / package.
        if (target.typeFqn() != null || target.packageName() != null) {
            String needle = target.typeFqn() != null ? target.typeFqn() : target.packageName();
            for (FileEntity file :
                    fileJpaRepository.findByRepositoryIdAndDirectoryFalse(target.repositoryId())) {
                for (FileImportEntity importEntity :
                        fileImportJpaRepository.findByFileId(file.getId())) {
                    if (importEntity.getImportName().equals(needle)
                            || importEntity.getImportName().startsWith(needle + ".")) {
                        items.add(
                                new ImpactItem(
                                        "FILE",
                                        file.getId().toString(),
                                        file.getPath(),
                                        1,
                                        "Imports " + importEntity.getImportName()));
                        evidence.add(
                                "Import evidence file="
                                        + file.getPath()
                                        + " import="
                                        + importEntity.getImportName());
                    }
                }
            }
        }

        int maxDepth = 0;
        Set<String> affectedClasses = new HashSet<>();
        Set<String> affectedPackages = new HashSet<>();
        Set<String> affectedFiles = new HashSet<>();

        while (!queue.isEmpty()) {
            DepthNode current = queue.poll();
            maxDepth = Math.max(maxDepth, current.depth());
            DependencyNodeEntity node = nodeById.get(current.nodeId());
            if (node == null) {
                continue;
            }
            if (node.getNodeType() == DependencyNodeType.CLASS) {
                affectedClasses.add(node.getLabel());
            } else if (node.getNodeType() == DependencyNodeType.PACKAGE) {
                affectedPackages.add(node.getLabel());
            } else if (node.getNodeType() == DependencyNodeType.FILE) {
                affectedFiles.add(node.getLabel());
            }
            if (current.depth() > 0) {
                items.add(
                        new ImpactItem(
                                node.getNodeType().name(),
                                node.getId().toString(),
                                node.getLabel(),
                                current.depth(),
                                "Reachable via dependency edge toward investigation target"));
            }
            for (DependencyEdgeEntity edge : incoming.getOrDefault(current.nodeId(), List.of())) {
                if (visited.add(edge.getSourceNodeId())) {
                    queue.add(new DepthNode(edge.getSourceNodeId(), current.depth() + 1));
                    evidence.add(
                            "Edge "
                                    + edge.getRelationship()
                                    + " source="
                                    + edge.getSourceNodeId()
                                    + " target="
                                    + edge.getTargetNodeId());
                }
            }
        }

        // Inheritance reverse lookup via superclassName / interfaces for class targets.
        if (target.typeFqn() != null) {
            for (CodeTypeEntity type :
                    codeTypeJpaRepository.findByRepositoryIdOrderByFullyQualifiedNameAsc(
                            target.repositoryId())) {
                boolean extendsTarget =
                        target.typeFqn().equals(type.getSuperclassName())
                                || (type.getSuperclassName() != null
                                        && type.getSuperclassName().endsWith(target.label()));
                if (extendsTarget) {
                    affectedClasses.add(type.getFullyQualifiedName());
                    items.add(
                            new ImpactItem(
                                    "CLASS",
                                    type.getId().toString(),
                                    type.getFullyQualifiedName(),
                                    1,
                                    "Extends/inherits target " + target.typeFqn()));
                    evidence.add(
                            "Inheritance evidence: "
                                    + type.getFullyQualifiedName()
                                    + " superclass="
                                    + type.getSuperclassName());
                }
            }
        }

        BigDecimal score =
                BigDecimal.valueOf(
                                Math.min(
                                        100,
                                        affectedClasses.size() * 2
                                                + affectedPackages.size() * 3
                                                + maxDepth * 5
                                                + affectedFiles.size()))
                        .setScale(3, RoundingMode.HALF_UP);

        return new ImpactResult(
                new ArrayList<>(affectedClasses),
                new ArrayList<>(affectedPackages),
                new ArrayList<>(affectedFiles),
                maxDepth,
                score,
                items,
                evidence);
    }

    private Set<String> seedKeys(InvestigationTarget target) {
        Set<String> keys = new HashSet<>();
        if (target.typeFqn() != null) {
            keys.add("class:" + target.typeFqn());
        }
        if (target.packageName() != null && !target.packageName().isBlank()) {
            keys.add("package:" + target.packageName());
        }
        if (target.filePath() != null) {
            keys.add("file:" + target.filePath());
        }
        return keys;
    }

    private record DepthNode(UUID nodeId, int depth) {}

    public record ImpactItem(
            String itemKind,
            String itemRef,
            String itemLabel,
            int dependencyDepth,
            String reason) {}

    public record ImpactResult(
            List<String> affectedClasses,
            List<String> affectedPackages,
            List<String> affectedFiles,
            int dependencyDepth,
            BigDecimal blastRadiusScore,
            List<ImpactItem> items,
            List<String> evidence) {}
}
