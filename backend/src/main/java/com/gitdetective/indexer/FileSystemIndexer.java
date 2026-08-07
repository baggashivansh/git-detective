package com.gitdetective.indexer;

import com.gitdetective.config.AnalysisProperties;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.parser.LanguageDetector;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.ignore.FastIgnoreRule;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileSystemIndexer {

    private final AnalysisProperties analysisProperties;
    private final LanguageDetector languageDetector;

    public List<IndexedFile> indexRepository(Path repositoryPath) {
        long started = System.currentTimeMillis();
        log.info("Filesystem index start path={}", repositoryPath);
        List<IndexedFile> indexed = new ArrayList<>();
        List<FastIgnoreRule> ignoreRules = loadIgnoreRules(repositoryPath);

        try {
            Files.walkFileTree(
                    repositoryPath,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(
                                Path dir, BasicFileAttributes attrs) {
                            String relative = relativize(repositoryPath, dir);
                            if (".git".equals(relative) || relative.startsWith(".git/")) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            if (!relative.isBlank()) {
                                indexed.add(
                                        toIndexedDirectory(
                                                repositoryPath, dir, attrs, ignoreRules));
                            }
                            if (indexed.size() > analysisProperties.maxFiles()) {
                                throw new RepositoryAnalysisException(
                                        "LARGE_REPOSITORY",
                                        "Repository exceeds configured max file limit of "
                                                + analysisProperties.maxFiles());
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            String relative = relativize(repositoryPath, file);
                            if (relative.startsWith(".git/") || ".git".equals(relative)) {
                                return FileVisitResult.CONTINUE;
                            }
                            indexed.add(toIndexedFile(repositoryPath, file, attrs, ignoreRules));
                            if (indexed.size() > analysisProperties.maxFiles()) {
                                throw new RepositoryAnalysisException(
                                        "LARGE_REPOSITORY",
                                        "Repository exceeds configured max file limit of "
                                                + analysisProperties.maxFiles());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (RepositoryAnalysisException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new RepositoryAnalysisException(
                    "SCAN_FAILED", "Failed to scan repository filesystem", exception);
        }

        log.info(
                "Filesystem index finish path={} entries={} durationMs={}",
                repositoryPath,
                indexed.size(),
                System.currentTimeMillis() - started);
        return indexed;
    }

    private IndexedFile toIndexedDirectory(
            Path root, Path dir, BasicFileAttributes attrs, List<FastIgnoreRule> ignoreRules) {
        String relative = relativize(root, dir);
        String name = dir.getFileName() == null ? relative : dir.getFileName().toString();
        return new IndexedFile(
                dir,
                relative,
                name,
                parentPath(relative),
                null,
                null,
                0,
                0,
                true,
                false,
                isHidden(name),
                isIgnored(relative, true, ignoreRules),
                null,
                toInstant(attrs.creationTime().toInstant()),
                toInstant(attrs.lastModifiedTime().toInstant()));
    }

    private IndexedFile toIndexedFile(
            Path root, Path file, BasicFileAttributes attrs, List<FastIgnoreRule> ignoreRules) {
        String relative = relativize(root, file);
        String name = file.getFileName().toString();
        boolean binary = isBinary(file);
        int lineCount = binary ? 0 : countLines(file);
        String hash = binary ? null : sha256(file);
        return new IndexedFile(
                file,
                relative,
                name,
                parentPath(relative),
                languageDetector.extensionOf(name),
                languageDetector.detectLanguage(name).orElse(null),
                attrs.size(),
                lineCount,
                false,
                binary,
                isHidden(name),
                isIgnored(relative, false, ignoreRules),
                hash,
                toInstant(attrs.creationTime().toInstant()),
                toInstant(attrs.lastModifiedTime().toInstant()));
    }

    private List<FastIgnoreRule> loadIgnoreRules(Path repositoryPath) {
        Path gitIgnore = repositoryPath.resolve(".gitignore");
        List<FastIgnoreRule> rules = new ArrayList<>();
        if (!Files.isRegularFile(gitIgnore)) {
            return rules;
        }
        try {
            for (String line : Files.readAllLines(gitIgnore, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                rules.add(new FastIgnoreRule(trimmed));
            }
        } catch (IOException ignored) {
            // Ignore unreadable gitignore; indexing continues without ignore rules.
        }
        return rules;
    }

    private boolean isIgnored(String relativePath, boolean directory, List<FastIgnoreRule> rules) {
        String path = relativePath.replace('\\', '/');
        Boolean ignored = null;
        for (FastIgnoreRule rule : rules) {
            if (rule.isMatch(path, directory)) {
                ignored = rule.getResult();
            }
        }
        return Boolean.TRUE.equals(ignored);
    }

    private boolean isBinary(Path file) {
        try (InputStream inputStream = Files.newInputStream(file)) {
            byte[] buffer = inputStream.readNBytes(8000);
            for (byte value : buffer) {
                if (value == 0) {
                    return true;
                }
            }
            return false;
        } catch (IOException exception) {
            return true;
        }
    }

    private int countLines(Path file) {
        try (var lines = Files.lines(file, StandardCharsets.UTF_8)) {
            return (int) lines.count();
        } catch (Exception exception) {
            return 0;
        }
    }

    private String sha256(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(file);
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (Exception exception) {
            return null;
        }
    }

    private String relativize(Path root, Path path) {
        return root.relativize(path).toString().replace('\\', '/');
    }

    private String parentPath(String relative) {
        int idx = relative.lastIndexOf('/');
        if (idx < 0) {
            return "";
        }
        return relative.substring(0, idx);
    }

    private boolean isHidden(String name) {
        return name != null && name.startsWith(".");
    }

    private Instant toInstant(Instant instant) {
        return instant;
    }
}
