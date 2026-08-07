package com.gitdetective.evidence.collector;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitdetective.evidence.EvidenceTestFixtures;
import com.gitdetective.evidence.mapper.InvestigationEvidenceMapper;
import com.gitdetective.evidence.model.EvidenceCategory;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CollectorsTest {

    private final InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();

    @Test
    @DisplayName("timeline collector emits timeline provenance at 100 confidence")
    void timelineCollector() {
        List<EvidenceRecord> records =
                new TimelineCollector(mapper)
                        .collect(
                                EvidenceTestFixtures.completedDetail(),
                                EvidenceTestFixtures.repository());
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().source()).isEqualTo(EvidenceProvenance.TIMELINE_ENGINE);
        assertThat(records.getFirst().confidence()).isEqualTo(100);
        assertThat(records.getFirst().evidenceType()).isEqualTo(EvidenceCategory.TIMELINE);
    }

    @Test
    @DisplayName("relationship collector emits graph provenance at 95 confidence")
    void relationshipCollector() {
        List<EvidenceRecord> records =
                new RelationshipCollector(mapper)
                        .collect(
                                EvidenceTestFixtures.completedDetail(),
                                EvidenceTestFixtures.repository());
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().source()).isEqualTo(EvidenceProvenance.RELATIONSHIP_ENGINE);
        assertThat(records.getFirst().confidence()).isEqualTo(95);
    }

    @Test
    @DisplayName("ownership collector emits contributor ownership evidence")
    void ownershipCollector() {
        List<EvidenceRecord> records =
                new OwnershipCollector(mapper)
                        .collect(
                                EvidenceTestFixtures.completedDetail(),
                                EvidenceTestFixtures.repository());
        assertThat(records).hasSize(1);
        assertThat(records.getFirst().sourceIdentifier()).isEqualTo("ada@example.com");
    }
}
