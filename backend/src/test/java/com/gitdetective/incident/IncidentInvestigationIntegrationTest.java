package com.gitdetective.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.gitdetective.analyzer.RepositoryCommandService;
import com.gitdetective.analyzer.RepositoryQueryService;
import com.gitdetective.config.AbstractIntegrationTest;
import com.gitdetective.dto.request.AnalyzeRepositoryRequest;
import com.gitdetective.dto.request.StartIncidentInvestigationRequest;
import com.gitdetective.dto.response.IncidentInvestigationResponse;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.RepositorySourceType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(
        properties = {
            "gitdetective.analysis.max-commits=1000",
            "gitdetective.analysis.max-files=1000",
            "gitdetective.ai.stub-mode=true"
        })
class IncidentInvestigationIntegrationTest extends AbstractIntegrationTest {

    @TempDir Path tempDir;

    @Autowired private RepositoryCommandService repositoryCommandService;

    @Autowired private RepositoryQueryService repositoryQueryService;

    @Autowired private IncidentInvestigationService incidentInvestigationService;

    @Test
    @DisplayName("runs question-driven investigation against a freshly analyzed local repository")
    void investigatesAnalyzedRepository() throws Exception {
        Path repo = tempDir.resolve("incident-repo");
        Files.createDirectories(repo);
        try (Git git = Git.init().setDirectory(repo.toFile()).call()) {
            Path javaFile = repo.resolve("src/com/example/AuthService.java");
            Files.createDirectories(javaFile.getParent());
            Files.writeString(
                    javaFile,
                    """
                    package com.example;

                    public class AuthService {
                        public boolean login(String user) {
                            return user != null;
                        }
                    }
                    """);
            git.add().addFilepattern(".").call();
            git.commit().setAuthor("Ada", "ada@example.com").setMessage("Add AuthService").call();
        }

        RepositorySummaryResponse queued =
                repositoryCommandService.analyze(
                        new AnalyzeRepositoryRequest(
                                RepositorySourceType.LOCAL, repo.toAbsolutePath().toString()));
        await().atMost(Duration.ofSeconds(60))
                .untilAsserted(
                        () ->
                                assertThat(
                                                repositoryQueryService
                                                        .getRepository(queued.id())
                                                        .status())
                                        .isEqualTo(AnalysisStatus.COMPLETED));

        IncidentInvestigationResponse started =
                incidentInvestigationService.start(
                        new StartIncidentInvestigationRequest(
                                queued.id(),
                                null,
                                null,
                                "Why did authentication become risky after recent changes?"));

        IncidentInvestigationResponse completed =
                started.phase() == IncidentPhase.COMPLETED
                        ? started
                        : incidentInvestigationService.advance(started.id());

        assertThat(completed.phase()).isEqualTo(IncidentPhase.COMPLETED);
        assertThat(completed.question()).contains("authentication");
        assertThat(completed.report()).isNotNull();
        assertThat(completed.report().timelineNote()).contains("not deployment");
        assertThat(completed.report().limitations()).isNotEmpty();
        assertThat(completed.report().claims()).isNotEmpty();
        assertThat(completed.report().finding()).isNotBlank();

        IncidentInvestigationResponse reused =
                incidentInvestigationService.start(
                        new StartIncidentInvestigationRequest(
                                queued.id(),
                                null,
                                null,
                                "Why did authentication become risky after recent changes?"));
        assertThat(reused.id()).isEqualTo(completed.id());
    }
}
