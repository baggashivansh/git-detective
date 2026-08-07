package com.gitdetective.impact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gitdetective.entity.DependencyEdgeEntity;
import com.gitdetective.entity.DependencyNodeEntity;
import com.gitdetective.entity.DependencyNodeType;
import com.gitdetective.entity.DependencyRelationship;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.DependencyEdgeJpaRepository;
import com.gitdetective.repository.DependencyNodeJpaRepository;
import com.gitdetective.repository.FileImportJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImpactEngineTest {

    @Mock private DependencyNodeJpaRepository dependencyNodeJpaRepository;

    @Mock private DependencyEdgeJpaRepository dependencyEdgeJpaRepository;

    @Mock private CodeTypeJpaRepository codeTypeJpaRepository;

    @Mock private FileJpaRepository fileJpaRepository;

    @Mock private FileImportJpaRepository fileImportJpaRepository;

    @InjectMocks private ImpactEngine impactEngine;

    @Test
    @DisplayName("computes blast radius from incoming dependency edges")
    void computesBlastRadius() {
        UUID repoId = UUID.randomUUID();
        UUID targetNode = UUID.randomUUID();
        UUID sourceNode = UUID.randomUUID();

        DependencyNodeEntity target =
                DependencyNodeEntity.builder()
                        .id(targetNode)
                        .repositoryId(repoId)
                        .nodeKey("class:com.example.Demo")
                        .nodeType(DependencyNodeType.CLASS)
                        .label("com.example.Demo")
                        .build();
        DependencyNodeEntity source =
                DependencyNodeEntity.builder()
                        .id(sourceNode)
                        .repositoryId(repoId)
                        .nodeKey("class:com.example.Caller")
                        .nodeType(DependencyNodeType.CLASS)
                        .label("com.example.Caller")
                        .build();
        DependencyEdgeEntity edge =
                DependencyEdgeEntity.builder()
                        .id(UUID.randomUUID())
                        .repositoryId(repoId)
                        .sourceNodeId(sourceNode)
                        .targetNodeId(targetNode)
                        .relationship(DependencyRelationship.IMPORT)
                        .build();

        when(dependencyNodeJpaRepository.findByRepositoryId(repoId))
                .thenReturn(List.of(target, source));
        when(dependencyEdgeJpaRepository.findByRepositoryId(repoId)).thenReturn(List.of(edge));
        when(fileJpaRepository.findByRepositoryIdAndDirectoryFalse(repoId)).thenReturn(List.of());
        when(codeTypeJpaRepository.findByRepositoryIdOrderByFullyQualifiedNameAsc(repoId))
                .thenReturn(List.of());

        InvestigationTarget investigationTarget =
                new InvestigationTarget(
                        InvestigationTargetType.CLASS,
                        targetNode.toString(),
                        "com.example.Demo",
                        repoId,
                        null,
                        null,
                        null,
                        "com.example",
                        targetNode,
                        "com.example.Demo",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);

        ImpactEngine.ImpactResult result = impactEngine.analyze(investigationTarget);

        assertThat(result.affectedClasses()).contains("com.example.Caller");
        assertThat(result.dependencyDepth()).isGreaterThanOrEqualTo(1);
        assertThat(result.blastRadiusScore()).isPositive();
        assertThat(result.evidence()).isNotEmpty();
    }
}
