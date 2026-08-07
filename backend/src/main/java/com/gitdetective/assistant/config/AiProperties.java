package com.gitdetective.assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gitdetective.ai")
public record AiProperties(
        String provider,
        String baseUrl,
        String apiKey,
        String model,
        double temperature,
        int maxTokens,
        boolean stubMode,
        int connectTimeoutMs,
        int readTimeoutMs) {

    public AiProperties {
        if (provider == null || provider.isBlank()) {
            provider = "openai-compatible";
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://api.openai.com/v1";
        }
        if (model == null || model.isBlank()) {
            model = "gpt-4o-mini";
        }
        if (maxTokens <= 0) {
            maxTokens = 2048;
        }
        if (connectTimeoutMs <= 0) {
            connectTimeoutMs = 10_000;
        }
        if (readTimeoutMs <= 0) {
            readTimeoutMs = 120_000;
        }
    }
}
