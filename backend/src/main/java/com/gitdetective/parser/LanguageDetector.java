package com.gitdetective.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class LanguageDetector {

    private static final Map<String, String> EXTENSION_LANGUAGE =
            Map.ofEntries(
                    Map.entry("java", "Java"),
                    Map.entry("kt", "Kotlin"),
                    Map.entry("kts", "Kotlin"),
                    Map.entry("ts", "TypeScript"),
                    Map.entry("tsx", "TypeScript"),
                    Map.entry("js", "JavaScript"),
                    Map.entry("jsx", "JavaScript"),
                    Map.entry("mjs", "JavaScript"),
                    Map.entry("cjs", "JavaScript"),
                    Map.entry("py", "Python"),
                    Map.entry("go", "Go"),
                    Map.entry("rs", "Rust"),
                    Map.entry("cs", "C#"),
                    Map.entry("cpp", "C++"),
                    Map.entry("cc", "C++"),
                    Map.entry("cxx", "C++"),
                    Map.entry("h", "C++"),
                    Map.entry("hpp", "C++"),
                    Map.entry("yml", "YAML"),
                    Map.entry("yaml", "YAML"),
                    Map.entry("json", "JSON"),
                    Map.entry("md", "Markdown"),
                    Map.entry("markdown", "Markdown"),
                    Map.entry("sql", "SQL"),
                    Map.entry("xml", "XML"),
                    Map.entry("properties", "Properties"),
                    Map.entry("gradle", "Gradle"),
                    Map.entry("toml", "TOML"),
                    Map.entry("sh", "Shell"),
                    Map.entry("bash", "Shell"));

    public Optional<String> detectLanguage(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.equals("dockerfile") || lower.endsWith("/dockerfile")) {
            return Optional.of("Dockerfile");
        }
        int dot = lower.lastIndexOf('.');
        if (dot < 0 || dot == lower.length() - 1) {
            return Optional.empty();
        }
        String extension = lower.substring(dot + 1);
        String language = EXTENSION_LANGUAGE.get(extension);
        return Optional.ofNullable(language).or(() -> Optional.of("Other"));
    }

    public String detectPrimaryLanguage(Path repositoryPath) throws IOException {
        Map<String, Long> counts = new HashMap<>();
        try (Stream<Path> stream = Files.walk(repositoryPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("/.git/"))
                    .forEach(
                            path ->
                                    detectLanguage(path.getFileName().toString())
                                            .ifPresent(
                                                    language ->
                                                            counts.merge(language, 1L, Long::sum)));
        }
        return counts.entrySet().stream()
                .filter(entry -> !"Other".equals(entry.getKey()))
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
