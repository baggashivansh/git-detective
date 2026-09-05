package com.gitdetective.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gitdetective.dto.response.IncidentInvestigationResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.exception.GlobalExceptionHandler;
import com.gitdetective.incident.IncidentInvestigationService;
import com.gitdetective.incident.IncidentPhase;
import com.gitdetective.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = IncidentInvestigationController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class IncidentInvestigationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private IncidentInvestigationService incidentInvestigationService;

    @Test
    @DisplayName("POST /incident-investigations starts an investigation")
    void startIncident() throws Exception {
        UUID id = UUID.randomUUID();
        when(incidentInvestigationService.start(any()))
                .thenReturn(sample(id, IncidentPhase.ANALYZING, InvestigationStatus.QUEUED));

        mockMvc.perform(
                        post("/incident-investigations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "repository": "https://github.com/acme/demo",
                                          "investigationQuestion": "Why did authentication become risky after recent changes?"
                                        }
                                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.phase").value("ANALYZING"))
                .andExpect(
                        jsonPath("$.data.question")
                                .value(org.hamcrest.Matchers.containsString("authentication")));
    }

    @Test
    @DisplayName("GET /incident-investigations/{id} returns current status")
    void getIncident() throws Exception {
        UUID id = UUID.randomUUID();
        when(incidentInvestigationService.get(id))
                .thenReturn(sample(id, IncidentPhase.COMPLETED, InvestigationStatus.COMPLETED));

        mockMvc.perform(get("/incident-investigations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phase").value("COMPLETED"));
    }

    @Test
    @DisplayName("POST /incident-investigations/{id}/continue advances the workflow")
    void continueIncident() throws Exception {
        UUID id = UUID.randomUUID();
        when(incidentInvestigationService.advance(id))
                .thenReturn(sample(id, IncidentPhase.INVESTIGATING, InvestigationStatus.RUNNING));

        mockMvc.perform(post("/incident-investigations/{id}/continue", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phase").value("INVESTIGATING"));
    }

    @Test
    @DisplayName("POST /incident-investigations rejects a blank question")
    void rejectsBlankQuestion() throws Exception {
        mockMvc.perform(
                        post("/incident-investigations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {"repository":"https://github.com/acme/demo","investigationQuestion":"   "}
                                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /incident-investigations lists incidents")
    void listIncidents() throws Exception {
        when(incidentInvestigationService.list()).thenReturn(List.of());

        mockMvc.perform(get("/incident-investigations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    private static IncidentInvestigationResponse sample(
            UUID id, IncidentPhase phase, InvestigationStatus status) {
        return new IncidentInvestigationResponse(
                id,
                UUID.randomUUID(),
                "demo",
                AnalysisStatus.COMPLETED,
                100,
                "Why did authentication become risky after recent changes?",
                InvestigationTargetType.BRANCH,
                "main",
                "main",
                status,
                phase,
                "summary",
                null,
                Instant.now(),
                Instant.now());
    }
}
