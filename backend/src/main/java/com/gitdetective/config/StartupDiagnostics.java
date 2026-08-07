package com.gitdetective.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Emits startup diagnostics without logging secrets. */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupDiagnostics {

    private final Environment environment;
    private final ApplicationProperties.GitDetectiveProperties properties;

    @PostConstruct
    void logStartup() {
        String version =
                properties.application() != null ? properties.application().version() : "unknown";
        String[] profiles = environment.getActiveProfiles();
        log.info(
                "Git Detective startup name={} version={} profiles={} java={}",
                environment.getProperty("spring.application.name", "git-detective"),
                version,
                profiles.length == 0 ? "default" : String.join(",", profiles),
                System.getProperty("java.version"));
    }
}
