package com.gitdetective.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.BusFactorLevel;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.evidence.builder.EvidenceBundleBuilder;
import com.gitdetective.evidence.cache.EvidenceBundleCache;
import com.gitdetective.evidence.cache.InMemoryEvidenceBundleCache;
import com.gitdetective.evidence.collector.RepositoryMetadataCollector;
import com.gitdetective.evidence.collector.StatisticsCollector;
import com.gitdetective.evidence.dto.EvidenceBundleRequest;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.validator.EvidenceValidator;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.investigation.InvestigationService;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvidenceBundleServiceTest {

    @Mock private InvestigationService investigationService;
    @Mock private CodeRepositoryJpaRepository codeRepositoryJpaRepository;

    private EvidenceBundleCache cache;
    private EvidenceBundleService service;

    @BeforeEach
    void setUp() {
        InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();
        EvidenceBundleBuilder builder =
                new EvidenceBundleBuilder(
                        List.of(
                                new RepositoryMetadataCollector(mapper),
                                new StatisticsCollector(mapper)),
                        new EvidenceValidator(),
                        mapper,
                        "1.0.0");
        cache = new InMemoryEvidenceBundleCache();
        service =
                new EvidenceBundleService(
                        investigationService, codeRepositoryJpaRepository, builder, cache);
    }

    @Test
    @DisplayName("builds and caches completed investigation bundles")
    void buildsAndCaches() {
        when(investigationService.get(EvidenceTestFixtures.INV_ID))
                .thenReturn(EvidenceTestFixtures.completedDetail());
        when(codeRepositoryJpaRepository.findById(EvidenceTestFixtures.REPO_ID))
                .thenReturn(Optional.of(EvidenceTestFixtures.repository()));

        EvidenceBundle first =
                service.build(new EvidenceBundleRequest(EvidenceTestFixtures.INV_ID));
        EvidenceBundle second =
                service.build(new EvidenceBundleRequest(EvidenceTestFixtures.INV_ID));

        assertThat(first.metadata().cached()).isFalse();
        assertThat(second.metadata().cached()).isTrue();
        assertThat(cache.size()).isEqualTo(1);
        verify(investigationService).get(EvidenceTestFixtures.INV_ID);
    }

    @Test
    @DisplayName("rejects incomplete investigations")
    void rejectsIncomplete() {
        InvestigationSummaryResponse summary =
                new InvestigationSummaryResponse(
                        EvidenceTestFixtures.INV_ID,
                        EvidenceTestFixtures.REPO_ID,
                        InvestigationTargetType.FILE,
                        "a.java",
                        "a.java",
                        InvestigationStatus.RUNNING,
                        null,
                        null,
                        null,
                        null,
                        Instant.now(),
                        null);
        when(investigationService.get(EvidenceTestFixtures.INV_ID))
                .thenReturn(
                        new InvestigationDetailResponse(
                                summary, List.of(), List.of(), List.of(), List.of(), List.of(),
                                List.of(), List.of(), List.of(), List.of()));

        assertThatThrownBy(
                        () -> service.build(new EvidenceBundleRequest(EvidenceTestFixtures.INV_ID)))
                .isInstanceOf(RepositoryAnalysisException.class);
    }

    @Test
    @DisplayName("bypassCache forces rebuild")
    void bypassCache() {
        when(investigationService.get(any())).thenReturn(EvidenceTestFixtures.completedDetail());
        when(codeRepositoryJpaRepository.findById(EvidenceTestFixtures.REPO_ID))
                .thenReturn(Optional.of(EvidenceTestFixtures.repository()));

        service.build(new EvidenceBundleRequest(EvidenceTestFixtures.INV_ID));
        EvidenceBundle fresh =
                service.build(new EvidenceBundleRequest(EvidenceTestFixtures.INV_ID, true));
        assertThat(fresh.metadata().cached()).isFalse();
        assertThat(fresh.ownership().busFactorLevel()).isIn(BusFactorLevel.HIGH.name(), "HIGH");
        assertThat(fresh.impact().blastRadiusScore())
                .isEqualByComparingTo(new BigDecimal("12.000"));
    }
}
