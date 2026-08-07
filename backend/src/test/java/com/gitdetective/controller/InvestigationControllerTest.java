package com.gitdetective.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.entity.BusFactorLevel;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.exception.GlobalExceptionHandler;
import com.gitdetective.investigation.InvestigationService;
import com.gitdetective.security.SecurityConfig;
import java.math.BigDecimal;
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
        controllers = InvestigationController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class InvestigationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private InvestigationService investigationService;

    @Test
    @DisplayName("POST /investigations creates investigation case")
    void createInvestigation() throws Exception {
        UUID id = UUID.randomUUID();
        when(investigationService.create(any()))
                .thenReturn(
                        new InvestigationSummaryResponse(
                                id,
                                UUID.randomUUID(),
                                InvestigationTargetType.CLASS,
                                "type-id",
                                "com.example.Demo",
                                InvestigationStatus.COMPLETED,
                                "summary",
                                1,
                                BusFactorLevel.HIGH,
                                BigDecimal.ONE,
                                Instant.now(),
                                Instant.now()));

        mockMvc.perform(
                        post("/investigations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "repositoryId": "%s",
                                          "targetType": "CLASS",
                                          "targetRef": "com.example.Demo"
                                        }
                                        """
                                                .formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    @DisplayName("GET /investigations/{id}/relationships returns relationship view")
    void getRelationships() throws Exception {
        UUID id = UUID.randomUUID();
        when(investigationService.relationships(id))
                .thenReturn(
                        new InvestigationDetailResponse(
                                new InvestigationSummaryResponse(
                                        id,
                                        UUID.randomUUID(),
                                        InvestigationTargetType.CLASS,
                                        "type-id",
                                        "com.example.Demo",
                                        InvestigationStatus.COMPLETED,
                                        "summary",
                                        2,
                                        BusFactorLevel.MEDIUM,
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
                                List.of()));

        mockMvc.perform(get("/investigations/{id}/relationships", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.id").value(id.toString()));
    }
}
