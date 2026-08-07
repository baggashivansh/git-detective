package com.gitdetective.investigation;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationReportResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.BusFactorLevel;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InvestigationReportExporterTest {

    private final InvestigationReportExporter exporter =
            new InvestigationReportExporter(
                    new ObjectMapper().registerModule(new JavaTimeModule()));

    @Test
    @DisplayName("exports factual json markdown and html reports")
    void exportsFormats() {
        InvestigationDetailResponse detail =
                new InvestigationDetailResponse(
                        new InvestigationSummaryResponse(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                InvestigationTargetType.CLASS,
                                "id",
                                "com.example.Demo",
                                InvestigationStatus.COMPLETED,
                                "Deterministic summary",
                                1,
                                BusFactorLevel.HIGH,
                                BigDecimal.TEN,
                                Instant.now(),
                                Instant.now()),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of());

        InvestigationReportResponse json = exporter.export(detail, "json");
        InvestigationReportResponse markdown = exporter.export(detail, "markdown");
        InvestigationReportResponse html = exporter.export(detail, "html");

        assertThat(json.content()).contains("com.example.Demo");
        assertThat(markdown.content()).contains("# Investigation Report");
        assertThat(html.content()).contains("<!DOCTYPE html>");
        assertThat(html.content()).contains("Deterministic summary");
    }
}
