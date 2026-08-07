package com.gitdetective.evidence.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.evidence.builder.EvidenceBundleBuilder;
import com.gitdetective.evidence.collector.RepositoryMetadataCollector;
import com.gitdetective.evidence.collector.StatisticsCollector;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.validator.EvidenceValidator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryEvidenceBundleCacheTest {

    @Test
    @DisplayName("stores and invalidates bundles by investigation id")
    void cacheLifecycle() {
        InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();
        EvidenceBundleBuilder builder =
                new EvidenceBundleBuilder(
                        List.of(
                                new RepositoryMetadataCollector(mapper),
                                new StatisticsCollector(mapper)),
                        new EvidenceValidator(),
                        mapper,
                        "1.0.0");
        EvidenceBundle bundle =
                builder.build(
                        EvidenceTestFixtures.completedDetail(), EvidenceTestFixtures.repository());

        EvidenceBundleCache cache = new InMemoryEvidenceBundleCache();
        cache.put(EvidenceTestFixtures.INV_ID, bundle);

        assertThat(cache.get(EvidenceTestFixtures.INV_ID)).contains(bundle);
        assertThat(cache.size()).isEqualTo(1);

        cache.invalidate(EvidenceTestFixtures.INV_ID);
        assertThat(cache.get(EvidenceTestFixtures.INV_ID)).isEmpty();
        assertThat(cache.size()).isZero();
    }
}
