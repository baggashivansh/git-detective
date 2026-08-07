package com.gitdetective.git;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gitdetective.config.AnalysisProperties;
import com.gitdetective.entity.RepositorySourceType;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.parser.LanguageDetector;
import java.nio.file.Files;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitEngineTest {

    @TempDir Path tempDir;

    private GitEngine gitEngine;

    @BeforeEach
    void setUp() {
        AnalysisProperties properties =
                new AnalysisProperties(
                        tempDir.resolve("workspaces").toString(), 60, 50_000_000, 5000, 5000);
        gitEngine = new GitEngine(properties, new LanguageDetector());
    }

    @Test
    @DisplayName("collects metadata from a local git repository")
    void collectsLocalRepositoryMetadata() throws Exception {
        Path repo = tempDir.resolve("sample-repo");
        Files.createDirectories(repo);
        try (Git git = Git.init().setDirectory(repo.toFile()).call()) {
            Path file = repo.resolve("Hello.java");
            Files.writeString(file, "public class Hello {}");
            git.add().addFilepattern("Hello.java").call();
            git.commit().setAuthor("Ada", "ada@example.com").setMessage("Initial commit").call();
        }

        Path workspace = tempDir.resolve("ws");
        Files.createDirectories(workspace);
        Path materialized =
                gitEngine.materializeRepository(
                        RepositorySourceType.LOCAL, repo.toString(), workspace);
        GitRepositorySnapshot snapshot = gitEngine.collectSnapshot(materialized);

        assertThat(snapshot.totalCommits()).isEqualTo(1);
        assertThat(snapshot.commits()).hasSize(1);
        assertThat(snapshot.commits().getFirst().authorEmail()).isEqualTo("ada@example.com");
        assertThat(snapshot.detectedPrimaryLanguage()).isEqualTo("Java");
        assertThat(snapshot.defaultBranch()).isNotBlank();
    }

    @Test
    @DisplayName("rejects unsupported non-GitHub remotes")
    void rejectsUnsupportedUrls() {
        assertThatThrownBy(() -> gitEngine.normalizeGitHubUrl("https://gitlab.com/a/b"))
                .isInstanceOf(RepositoryAnalysisException.class)
                .hasMessageContaining("github.com");
    }
}
