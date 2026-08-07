package com.gitdetective.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gitdetective.dto.response.HealthResponse;
import com.gitdetective.exception.GlobalExceptionHandler;
import com.gitdetective.security.SecurityConfig;
import com.gitdetective.service.HealthService;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = HealthController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class HealthControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private HealthService healthService;

    @Test
    @DisplayName("GET /health returns application health payload")
    void healthEndpointReturnsExpectedPayload() throws Exception {
        when(healthService.getHealth())
                .thenReturn(
                        HealthResponse.builder()
                                .status("UP")
                                .application("git-detective")
                                .version("1.0.0")
                                .timestamp(Instant.parse("2026-08-07T06:30:00Z"))
                                .build());

        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.application").value("git-detective"))
                .andExpect(jsonPath("$.data.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.timestamp").value("2026-08-07T06:30:00Z"));
    }
}
