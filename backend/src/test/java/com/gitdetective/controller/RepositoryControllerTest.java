package com.gitdetective.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gitdetective.analyzer.RepositoryCommandService;
import com.gitdetective.analyzer.RepositoryQueryService;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.dto.response.SearchResultResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.exception.GlobalExceptionHandler;
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
        controllers = RepositoryController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class RepositoryControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private RepositoryCommandService repositoryCommandService;

    @MockitoBean private RepositoryQueryService repositoryQueryService;

    @Test
    @DisplayName("POST /repositories/analyze returns accepted repository summary")
    void analyzeRepository() throws Exception {
        UUID id = UUID.randomUUID();
        when(repositoryCommandService.analyze(any()))
                .thenReturn(
                        new RepositorySummaryResponse(
                                id,
                                "demo",
                                RepositorySourceType.GITHUB,
                                "https://github.com/octocat/Hello-World",
                                null,
                                null,
                                0,
                                0,
                                null,
                                AnalysisStatus.QUEUED,
                                "Queued for analysis",
                                0,
                                null,
                                null,
                                null,
                                Instant.now(),
                                Instant.now(),
                                null));

        mockMvc.perform(
                        post("/repositories/analyze")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "sourceType": "GITHUB",
                                          "source": "https://github.com/octocat/Hello-World"
                                        }
                                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.status").value("QUEUED"));
    }

    @Test
    @DisplayName("GET /repositories/{id}/search returns search payload")
    void searchRepository() throws Exception {
        UUID id = UUID.randomUUID();
        when(repositoryQueryService.search(id, "Main"))
                .thenReturn(
                        new SearchResultResponse(
                                "Main",
                                List.of(
                                        new SearchResultResponse.SearchHit(
                                                "file", "1", "Main.java", "src/Main.java")),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of()));

        mockMvc.perform(get("/repositories/{id}/search", id).param("q", "Main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.query").value("Main"))
                .andExpect(jsonPath("$.data.files[0].label").value("Main.java"));
    }
}
