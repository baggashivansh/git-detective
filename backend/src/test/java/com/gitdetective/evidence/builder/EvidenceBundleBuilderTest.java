package com.gitdetective.evidence.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.evidence.collector.CommitClusterCollector;
import com.gitdetective.evidence.collector.DependencyCollector;
import com.gitdetective.evidence.collector.HotspotCollector;
import com.gitdetective.evidence.collector.ImpactCollector;
import com.gitdetective.evidence.collector.InvestigationEvidenceCollector;
import com.gitdetective.evidence.collector.OwnershipCollector;
import com.gitdetective.evidence.collector.PackageHealthCollector;
import com.gitdetective.evidence.collector.RelationshipCollector;
import com.gitdetective.evidence.collector.RepositoryMetadataCollector;
import com.gitdetective.evidence.collector.StatisticsCollector;
import com.gitdetective.evidence.collector.TimelineCollector;
import com.gitdetective.evidence.collector.TraceCollector;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.model.EvidenceVerificationStatus;
import com.gitdetective.evidence.validator.EvidenceValidator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EvidenceBundleBuilderTest {

    private EvidenceBundleBuilder builder;

    @BeforeEach
    void setUp() {
        InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();
        EvidenceValidator validator = new EvidenceValidator();
        builder =
                new EvidenceBundleBuilder(
                        List.of(
                                new RepositoryMetadataCollector(mapper),
                                new InvestigationEvidenceCollector(mapper),
                                new TimelineCollector(mapper),
                                new OwnershipCollector(mapper),
                                new ImpactCollector(mapper),
                                new RelationshipCollector(mapper),
                                new DependencyCollector(mapper),
                                new HotspotCollector(mapper),
                                new PackageHealthCollector(mapper),
                                new StatisticsCollector(mapper),
                                new TraceCollector(mapper),
                                new CommitClusterCollector(mapper)),
                        validator,
                        mapper,
                        "1.0.0");
    }

    @Test
    @DisplayName("builds complete verified evidence bundle from investigation detail")
    void buildsCompleteBundle() {
        EvidenceBundle bundle =
                builder.build(
                        EvidenceTestFixtures.completedDetail(), EvidenceTestFixtures.repository());

        assertThat(bundle.investigationId()).isEqualTo(EvidenceTestFixtures.INV_ID);
        assertThat(bundle.repositoryId()).isEqualTo(EvidenceTestFixtures.REPO_ID);
        assertThat(bundle.allEvidence()).isNotEmpty();
        assertThat(bundle.allEvidence())
                .allMatch(r -> r.verificationStatus() == EvidenceVerificationStatus.VERIFIED);
        assertThat(bundle.timeline().events()).isNotEmpty();
        assertThat(bundle.ownership().owners()).isNotEmpty();
        assertThat(bundle.impact().items()).isNotEmpty();
        assertThat(bundle.relationships().relationships()).isNotEmpty();
        assertThat(bundle.hotspots().hotspots()).isNotEmpty();
        assertThat(bundle.packageHealth().packages()).isNotEmpty();
        assertThat(bundle.statistics().timelineCount()).isEqualTo(1);
        assertThat(bundle.metadata().engineVersion()).isEqualTo("1.0.0");
        assertThat(bundle.metadata().cached()).isFalse();
        assertThat(bundle.evidenceSummary().factualOverview()).contains("Demo");
        assertThat(bundle.supportingContributors()).isNotEmpty();
    }

    @Test
    @DisplayName("rejects repository mismatch")
    void rejectsRepositoryMismatch() {
        var wrongRepo = EvidenceTestFixtures.repository();
        wrongRepo.setId(java.util.UUID.randomUUID());

        assertThatThrownBy(() -> builder.build(EvidenceTestFixtures.completedDetail(), wrongRepo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Repository mismatch");
    }

    @Test
    @DisplayName("asCachedHit flips metadata flag without changing evidence")
    void cachedHitCopy() {
        EvidenceBundle bundle =
                builder.build(
                        EvidenceTestFixtures.completedDetail(), EvidenceTestFixtures.repository());
        EvidenceBundle hit = bundle.asCachedHit();
        assertThat(hit.metadata().cached()).isTrue();
        assertThat(hit.allEvidence()).hasSize(bundle.allEvidence().size());
    }
}
