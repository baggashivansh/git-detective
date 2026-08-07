package com.gitdetective.graph;

import com.gitdetective.entity.DependencyEdgeEntity;
import com.gitdetective.entity.DependencyNodeEntity;
import com.gitdetective.entity.DependencyNodeType;
import com.gitdetective.entity.DependencyRelationship;
import com.gitdetective.parser.ParsedJavaFile;
import com.gitdetective.repository.DependencyEdgeJpaRepository;
import com.gitdetective.repository.DependencyNodeJpaRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Persists import/inheritance/implementation/package relationships without visualization. */
@Component
@RequiredArgsConstructor
public class DependencyGraphBuilder {

    private final DependencyNodeJpaRepository dependencyNodeJpaRepository;
    private final DependencyEdgeJpaRepository dependencyEdgeJpaRepository;

    public void buildForJavaFile(
            UUID repositoryId, String filePath, String packageName, ParsedJavaFile parsedJavaFile) {
        Map<String, UUID> nodeIds = new HashMap<>();

        UUID fileNodeId =
                upsertNode(
                        repositoryId,
                        "file:" + filePath,
                        DependencyNodeType.FILE,
                        filePath,
                        nodeIds);
        if (packageName != null && !packageName.isBlank()) {
            UUID packageNodeId =
                    upsertNode(
                            repositoryId,
                            "package:" + packageName,
                            DependencyNodeType.PACKAGE,
                            packageName,
                            nodeIds);
            upsertEdge(
                    repositoryId,
                    fileNodeId,
                    packageNodeId,
                    DependencyRelationship.PACKAGE_DEPENDENCY);
        }

        for (ParsedJavaFile.ImportInfo importInfo : parsedJavaFile.imports()) {
            UUID importNodeId =
                    upsertNode(
                            repositoryId,
                            "class:" + importInfo.name(),
                            DependencyNodeType.CLASS,
                            importInfo.name(),
                            nodeIds);
            upsertEdge(repositoryId, fileNodeId, importNodeId, DependencyRelationship.IMPORT);
        }

        for (ParsedJavaFile.TypeInfo type : parsedJavaFile.types()) {
            UUID typeNodeId =
                    upsertNode(
                            repositoryId,
                            "class:" + type.fullyQualifiedName(),
                            DependencyNodeType.CLASS,
                            type.fullyQualifiedName(),
                            nodeIds);
            if (type.superclassName() != null && !type.superclassName().isBlank()) {
                UUID superNodeId =
                        upsertNode(
                                repositoryId,
                                "class:" + type.superclassName(),
                                DependencyNodeType.CLASS,
                                type.superclassName(),
                                nodeIds);
                upsertEdge(
                        repositoryId, typeNodeId, superNodeId, DependencyRelationship.INHERITANCE);
            }
            for (String implemented : type.implementedInterfaces()) {
                UUID interfaceNodeId =
                        upsertNode(
                                repositoryId,
                                "class:" + implemented,
                                DependencyNodeType.CLASS,
                                implemented,
                                nodeIds);
                upsertEdge(
                        repositoryId,
                        typeNodeId,
                        interfaceNodeId,
                        DependencyRelationship.IMPLEMENTATION);
            }
        }
    }

    private UUID upsertNode(
            UUID repositoryId,
            String key,
            DependencyNodeType type,
            String label,
            Map<String, UUID> cache) {
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        UUID id =
                dependencyNodeJpaRepository
                        .findByRepositoryIdAndNodeKey(repositoryId, key)
                        .map(DependencyNodeEntity::getId)
                        .orElseGet(
                                () ->
                                        dependencyNodeJpaRepository
                                                .save(
                                                        DependencyNodeEntity.builder()
                                                                .repositoryId(repositoryId)
                                                                .nodeKey(key)
                                                                .nodeType(type)
                                                                .label(label)
                                                                .build())
                                                .getId());
        cache.put(key, id);
        return id;
    }

    private void upsertEdge(
            UUID repositoryId, UUID sourceId, UUID targetId, DependencyRelationship relationship) {
        boolean exists =
                dependencyEdgeJpaRepository
                        .findByRepositoryIdAndSourceNodeIdAndTargetNodeIdAndRelationship(
                                repositoryId, sourceId, targetId, relationship)
                        .isPresent();
        if (exists) {
            return;
        }
        dependencyEdgeJpaRepository.save(
                DependencyEdgeEntity.builder()
                        .repositoryId(repositoryId)
                        .sourceNodeId(sourceId)
                        .targetNodeId(targetId)
                        .relationship(relationship)
                        .build());
    }
}
