package com.gitdetective.relationship;

import com.gitdetective.entity.DependencyEdgeEntity;
import com.gitdetective.entity.DependencyNodeEntity;
import com.gitdetective.entity.DependencyRelationship;
import com.gitdetective.entity.InvestigationRelationshipType;
import com.gitdetective.entity.TypeInterfaceEntity;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.ownership.OwnershipEngine;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.DependencyEdgeJpaRepository;
import com.gitdetective.repository.DependencyNodeJpaRepository;
import com.gitdetective.repository.TypeInterfaceJpaRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RelationshipEngine {

    private final DependencyNodeJpaRepository dependencyNodeJpaRepository;
    private final DependencyEdgeJpaRepository dependencyEdgeJpaRepository;
    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final TypeInterfaceJpaRepository typeInterfaceJpaRepository;
    private final OwnershipEngine ownershipEngine;

    public List<RelationshipEdge> build(InvestigationTarget target) {
        List<RelationshipEdge> relationships = new ArrayList<>();
        List<DependencyNodeEntity> nodes =
                dependencyNodeJpaRepository.findByRepositoryId(target.repositoryId());
        Map<UUID, DependencyNodeEntity> byId = new HashMap<>();
        Map<String, DependencyNodeEntity> byKey = new HashMap<>();
        for (DependencyNodeEntity node : nodes) {
            byId.put(node.getId(), node);
            byKey.put(node.getNodeKey(), node);
        }

        List<DependencyEdgeEntity> edges =
                dependencyEdgeJpaRepository.findByRepositoryId(target.repositoryId());
        for (DependencyEdgeEntity edge : edges) {
            DependencyNodeEntity source = byId.get(edge.getSourceNodeId());
            DependencyNodeEntity dest = byId.get(edge.getTargetNodeId());
            if (source == null || dest == null) {
                continue;
            }
            if (!isRelevant(target, source, dest)) {
                continue;
            }
            relationships.add(
                    new RelationshipEdge(
                            source.getNodeKey(),
                            source.getLabel(),
                            source.getNodeType().name(),
                            dest.getNodeKey(),
                            dest.getLabel(),
                            dest.getNodeType().name(),
                            mapRelationship(edge.getRelationship()),
                            "dependency_edge:" + edge.getId()));
        }

        if (target.typeId() != null) {
            codeTypeJpaRepository
                    .findById(target.typeId())
                    .ifPresent(
                            type -> {
                                if (type.getSuperclassName() != null) {
                                    relationships.add(
                                            new RelationshipEdge(
                                                    "class:" + type.getFullyQualifiedName(),
                                                    type.getFullyQualifiedName(),
                                                    "CLASS",
                                                    "class:" + type.getSuperclassName(),
                                                    type.getSuperclassName(),
                                                    "CLASS",
                                                    InvestigationRelationshipType.EXTENDS,
                                                    "code_type.superclass:" + type.getId()));
                                }
                                for (TypeInterfaceEntity iface :
                                        typeInterfaceJpaRepository.findByTypeId(type.getId())) {
                                    relationships.add(
                                            new RelationshipEdge(
                                                    "class:" + type.getFullyQualifiedName(),
                                                    type.getFullyQualifiedName(),
                                                    "CLASS",
                                                    "class:" + iface.getInterfaceName(),
                                                    iface.getInterfaceName(),
                                                    "CLASS",
                                                    InvestigationRelationshipType.IMPLEMENTS,
                                                    "type_interface:"
                                                            + type.getId()
                                                            + ":"
                                                            + iface.getInterfaceName()));
                                }
                                if (type.getPackageId() != null && target.packageName() != null) {
                                    relationships.add(
                                            new RelationshipEdge(
                                                    "class:" + type.getFullyQualifiedName(),
                                                    type.getFullyQualifiedName(),
                                                    "CLASS",
                                                    "package:" + target.packageName(),
                                                    target.packageName(),
                                                    "PACKAGE",
                                                    InvestigationRelationshipType.BELONGS_TO,
                                                    "code_type.package:" + type.getId()));
                                }
                            });
        }

        OwnershipEngine.OwnershipResult ownership = ownershipEngine.calculate(target);
        for (OwnershipEngine.OwnerShare owner : ownership.owners()) {
            relationships.add(
                    new RelationshipEdge(
                            "contributor:" + owner.email(),
                            owner.name(),
                            "CONTRIBUTOR",
                            target.ref(),
                            target.label(),
                            target.type().name(),
                            InvestigationRelationshipType.OWNS,
                            "ownership:" + owner.email()));
            relationships.add(
                    new RelationshipEdge(
                            target.ref(),
                            target.label(),
                            target.type().name(),
                            "contributor:" + owner.email(),
                            owner.name(),
                            "CONTRIBUTOR",
                            InvestigationRelationshipType.MODIFIED_BY,
                            "ownership:" + owner.email()));
        }

        return relationships;
    }

    private boolean isRelevant(
            InvestigationTarget target, DependencyNodeEntity source, DependencyNodeEntity dest) {
        String fqn = target.typeFqn();
        String pkg = target.packageName();
        String path = target.filePath();
        return contains(source, fqn, pkg, path) || contains(dest, fqn, pkg, path);
    }

    private boolean contains(DependencyNodeEntity node, String fqn, String pkg, String path) {
        String label = node.getLabel();
        String key = node.getNodeKey();
        return (fqn != null && (fqn.equals(label) || key.equals("class:" + fqn)))
                || (pkg != null && (pkg.equals(label) || key.equals("package:" + pkg)))
                || (path != null && (path.equals(label) || key.equals("file:" + path)));
    }

    private InvestigationRelationshipType mapRelationship(DependencyRelationship relationship) {
        return switch (relationship) {
            case IMPORT -> InvestigationRelationshipType.IMPORTS;
            case INHERITANCE -> InvestigationRelationshipType.EXTENDS;
            case IMPLEMENTATION -> InvestigationRelationshipType.IMPLEMENTS;
            case PACKAGE_DEPENDENCY -> InvestigationRelationshipType.BELONGS_TO;
        };
    }

    public record RelationshipEdge(
            String sourceKey,
            String sourceLabel,
            String sourceType,
            String targetKey,
            String targetLabel,
            String targetType,
            InvestigationRelationshipType relationshipType,
            String evidenceRef) {}
}
