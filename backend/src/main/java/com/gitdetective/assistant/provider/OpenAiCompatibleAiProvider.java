package com.gitdetective.assistant.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gitdetective.assistant.config.AiProperties;
import com.gitdetective.assistant.prompt.PromptBuilder.PromptPayload;
import com.gitdetective.exception.RepositoryAnalysisException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Single AI provider implementation: OpenAI-compatible Chat Completions HTTP API.
 *
 * <p>When {@code gitdetective.ai.stub-mode=true} (or API key blank), produces a deterministic
 * evidence-backed JSON answer without calling an external vendor — useful for local/CI.
 *
 * <p>Easily replaceable: swap this bean for another {@link AiProvider} implementation.
 */
@Slf4j
@Component
public class OpenAiCompatibleAiProvider implements AiProvider {

    private final AiProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiCompatibleAiProvider(AiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                        .build();
    }

    @Override
    public String name() {
        return properties.stubMode() || isBlank(properties.apiKey())
                ? "openai-compatible-stub"
                : "openai-compatible";
    }

    @Override
    public String complete(PromptPayload prompt) {
        if (useStub()) {
            return stubComplete(prompt);
        }
        return callChatCompletions(prompt, false);
    }

    @Override
    public void stream(
            PromptPayload prompt, Consumer<String> onToken, Consumer<String> onComplete) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        if (useStub()) {
            String full = stubComplete(prompt);
            streamLocally(full, onToken, onComplete);
            return;
        }
        String full = callChatCompletions(prompt, false);
        streamLocally(full, onToken, onComplete);
    }

    private boolean useStub() {
        return properties.stubMode() || isBlank(properties.apiKey());
    }

    private void streamLocally(String full, Consumer<String> onToken, Consumer<String> onComplete) {
        int chunk = Math.max(24, full.length() / 20);
        for (int i = 0; i < full.length(); i += chunk) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            int end = Math.min(full.length(), i + chunk);
            onToken.accept(full.substring(i, end));
            try {
                Thread.sleep(8);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        onComplete.accept(full);
    }

    private String callChatCompletions(PromptPayload prompt, boolean stream) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", properties.model());
            body.put("temperature", properties.temperature());
            body.put("max_tokens", properties.maxTokens());
            body.put("stream", stream);
            ArrayNode messages = body.putArray("messages");
            messages.addObject().put("role", "system").put("content", prompt.systemPrompt());
            messages.addObject()
                    .put("role", "system")
                    .put(
                            "content",
                            prompt.developerInstructions() + "\n\n" + prompt.evidenceContext());
            messages.addObject().put("role", "user").put("content", prompt.userQuestion());

            String url = trimSlash(properties.baseUrl()) + "/chat/completions";
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                            .header("Authorization", "Bearer " + properties.apiKey())
                            .header("Content-Type", "application/json")
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            objectMapper.writeValueAsString(body),
                                            StandardCharsets.UTF_8))
                            .build();

            log.info(
                    "AI provider execution start provider={} model={} stream={}",
                    name(),
                    properties.model(),
                    stream);
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.error("AI provider failure status={}", response.statusCode());
                throw new RepositoryAnalysisException(
                        HttpStatus.BAD_GATEWAY,
                        "AI_PROVIDER_ERROR",
                        "AI provider returned status " + response.statusCode());
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content =
                    root.path("choices").path(0).path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new RepositoryAnalysisException(
                        HttpStatus.BAD_GATEWAY,
                        "AI_PROVIDER_EMPTY",
                        "AI provider returned an empty completion");
            }
            log.info("AI provider execution finish provider={} chars={}", name(), content.length());
            return content;
        } catch (RepositoryAnalysisException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("AI provider execution failed: {}", ex.getMessage());
            throw new RepositoryAnalysisException(
                    HttpStatus.BAD_GATEWAY, "AI_PROVIDER_ERROR", "Failed to call AI provider", ex);
        }
    }

    /**
     * Deterministic evidence-backed answer used when stub mode is on. Still JSON-shaped for the
     * validator/formatter pipeline.
     */
    String stubComplete(PromptPayload prompt) {
        List<String> ids = new ArrayList<>();
        for (String line : prompt.evidenceContext().split("\n")) {
            int idIdx = line.indexOf("[id=");
            if (idIdx >= 0) {
                int end = line.indexOf(' ', idIdx + 4);
                if (end > idIdx) {
                    String id = line.substring(idIdx + 4, end);
                    try {
                        UUID.fromString(id);
                        ids.add(id);
                    } catch (IllegalArgumentException ignored) {
                        // skip
                    }
                }
            }
            if (ids.size() >= 8) {
                break;
            }
        }

        String answer;
        if (ids.isEmpty()) {
            answer =
                    "The available repository evidence is insufficient to answer this confidently.";
        } else {
            answer =
                    "Based on indexed investigation evidence for intent "
                            + prompt.intent()
                            + " regarding \""
                            + prompt.userQuestion()
                            + "\": "
                            + summarizeEvidenceLines(prompt.evidenceContext())
                            + " All statements above are derived from the cited evidence identifiers.";
        }

        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("answer", answer);
            ArrayNode evidenceIds = node.putArray("evidenceIds");
            ids.forEach(evidenceIds::add);
            node.put("confidence", ids.isEmpty() ? 0 : 90);
            node.putArray("referencedFiles");
            node.putArray("referencedCommits");
            node.putArray("referencedContributors");
            node.putArray("referencedPackages");
            return objectMapper.writeValueAsString(node);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build stub AI response", ex);
        }
    }

    private static String summarizeEvidenceLines(String evidenceContext) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String line : evidenceContext.split("\n")) {
            if (line.startsWith("[id=")) {
                int desc = line.indexOf("] ");
                if (desc > 0) {
                    sb.append(line.substring(desc + 2).trim());
                    sb.append(' ');
                    count++;
                }
            }
            if (count >= 5) {
                break;
            }
        }
        return sb.toString().trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String trimSlash(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}
