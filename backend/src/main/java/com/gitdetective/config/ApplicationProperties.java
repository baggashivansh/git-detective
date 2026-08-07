package com.gitdetective.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ApplicationProperties.GitDetectiveProperties.class)
public class ApplicationProperties {

    @ConfigurationProperties(prefix = "gitdetective")
    public record GitDetectiveProperties(Application application, Cors cors) {

        public record Application(String version) {}

        public record Cors(List<String> allowedOrigins) {}
    }
}
