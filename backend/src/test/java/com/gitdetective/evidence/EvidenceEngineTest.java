package com.gitdetective.evidence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gitdetective.evidence.builder.EvidenceBundleBuilder;
import com.gitdetective.evidence.collector.RepositoryMetadataCollector;
import com.gitdetective.evidence.collector.StatisticsCollector;
import com.gitdetective.evidence.dto.EvidenceBundleRequest;
import com.gitdetective.evidence.dto.EvidenceBundleView;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.service.EvidenceBundleService;
import com.gitdetective.evidence.validator.EvidenceValidator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvidenceEngineTest {

    @Mock private EvidenceBundleService evidenceBundleService;

    @InjectMocks private EvidenceEngine evidenceEngine;

    @Test
    @DisplayName("facade delegates gather and prepareForAi")
    void delegates() {
        InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();
        EvidenceBundle bundle =
                new EvidenceBundleBuilder(
                                List.of(
                                        new RepositoryMetadataCollector(mapper),
                                        new StatisticsCollector(mapper)),
                                new EvidenceValidator(),
                                mapper,
                                "1.0.0")
                        .build(
                                EvidenceTestFixtures.completedDetail(),
                                EvidenceTestFixtures.repository());

        when(evidenceBundleService.build(new EvidenceBundleRequest(EvidenceTestFixtures.INV_ID)))
                .thenReturn(bundle);
        when(evidenceBundleService.view(EvidenceTestFixtures.INV_ID))
                .thenReturn(EvidenceBundleView.from(bundle));

        assertThat(evidenceEngine.gather(EvidenceTestFixtures.INV_ID)).isSameAs(bundle);
        assertThat(evidenceEngine.prepareForAi(EvidenceTestFixtures.INV_ID).bundle())
                .isSameAs(bundle);

        evidenceEngine.invalidate(EvidenceTestFixtures.INV_ID);
        verify(evidenceBundleService).invalidate(EvidenceTestFixtures.INV_ID);
    }
}
