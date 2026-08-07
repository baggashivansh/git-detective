package com.gitdetective.analyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.gitdetective.config.AbstractIntegrationTest;
import com.gitdetective.dto.request.AnalyzeRepositoryRequest;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.entity.AnalysisStatus;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.repository.CommitJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
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
            "gitdetective.analysis.max-files=1000"
        })
class RepositoryAnalysisIntegrationTest extends AbstractIntegrationTest {

    @TempDir Path tempDir;

    @Autowired private RepositoryCommandService repositoryCommandService;

    @Autowired private RepositoryQueryService repositoryQueryService;

    @Autowired private CommitJpaRepository commitJpaRepository;

    @Autowired private FileJpaRepository fileJpaRepository;

    @Test
    @DisplayName("analyzes a local repository end-to-end and indexes commits/files")
    void analyzesLocalRepository() throws Exception {
        Path repo = tempDir.resolve("local-repo");
        Files.createDirectories(repo);
        try (Git git = Git.init().setDirectory(repo.toFile()).call()) {
            Path javaFile = repo.resolve("src/com/example/Demo.java");
            Files.createDirectories(javaFile.getParent());
            Files.writeString(
                    javaFile,
                    """
                    package com.example;

                    public class Demo {
                        public void run() {}
                    }
                    """);
            git.add().addFilepattern(".").call();
            git.commit()
                    .setAuthor("Grace", "grace@example.com")
                    .setMessage("Add Demo class")
                    .call();
        }

        RepositorySummaryResponse queued =
                repositoryCommandService.analyze(
                        new AnalyzeRepositoryRequest(
                                RepositorySourceType.LOCAL, repo.toAbsolutePath().toString()));

        await().atMost(Duration.ofSeconds(60))
                .untilAsserted(
                        () -> {
                            RepositorySummaryResponse current =
                                    repositoryQueryService.getRepository(queued.id());
                            assertThat(current.status())
                                    .isIn(AnalysisStatus.COMPLETED, AnalysisStatus.FAILED);
                            assertThat(current.status()).isEqualTo(AnalysisStatus.COMPLETED);
                        });

        RepositorySummaryResponse completed = repositoryQueryService.getRepository(queued.id());
        assertThat(completed.totalCommits()).isEqualTo(1);
        assertThat(commitJpaRepository.countByRepositoryId(completed.id())).isEqualTo(1);
        assertThat(fileJpaRepository.findByRepositoryIdAndDirectoryFalse(completed.id()))
                .anyMatch(file -> file.getPath().endsWith("Demo.java"));
        assertThat(repositoryQueryService.getPackages(completed.id()))
                .extracting(response -> response.name())
                .contains("com.example");
        assertThat(repositoryQueryService.search(completed.id(), "Demo").classes()).isNotEmpty();
    }
}
