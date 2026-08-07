package com.gitdetective.evidence.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitdetective.evidence.model.EvidenceCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvestigationEvidenceMapperTest {

    private final InvestigationEvidenceMapper mapper = new InvestigationEvidenceMapper();

    @Test
    @DisplayName("maps known evidence types")
    void mapsEvidenceTypes() {
        assertThat(mapper.mapEvidenceType("COMMIT")).isEqualTo(EvidenceCategory.COMMIT);
        assertThat(mapper.mapEvidenceType("TIMELINE")).isEqualTo(EvidenceCategory.TIMELINE);
        assertThat(mapper.mapEvidenceType("unknown-x")).isEqualTo(EvidenceCategory.STATISTIC);
    }

    @Test
    @DisplayName("maps target types")
    void mapsTargetTypes() {
        assertThat(mapper.mapTargetType("CLASS")).isEqualTo(EvidenceCategory.CLASS);
        assertThat(mapper.mapTargetType("BRANCH")).isEqualTo(EvidenceCategory.TARGET);
    }

    @Test
    @DisplayName("requireRef prefers non-blank values")
    void requireRef() {
        assertThat(mapper.requireRef("a", "b")).isEqualTo("a");
        assertThat(mapper.requireRef(" ", "b")).isEqualTo("b");
        assertThat(mapper.requireRef(null, null)).isEqualTo("unknown");
    }
}
