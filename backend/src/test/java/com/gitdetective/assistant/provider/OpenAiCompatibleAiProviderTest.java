package com.gitdetective.assistant.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.assistant.config.AiProperties;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.prompt.PromptBuilder.PromptPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OpenAiCompatibleAiProviderTest {

    @Test
    @DisplayName("stub mode returns JSON citing evidence ids from context")
    void stubComplete() {
        AiProperties props =
                new AiProperties(
                        "openai-compatible",
                        "https://api.openai.com/v1",
                        "",
                        "gpt-4o-mini",
                        0.0,
                        512,
                        true,
                        1000,
                        1000);
        OpenAiCompatibleAiProvider provider =
                new OpenAiCompatibleAiProvider(props, new ObjectMapper());

        String evidence =
                "[id=11111111-1111-1111-1111-111111111111 type=FILE confidence=100 provenance=GIT_COMMIT] Created file ref=a.java\n";
        PromptPayload prompt =
                new PromptPayload(
                        "system",
                        "developer",
                        "Overview: x\nEvidence:\n" + evidence,
                        "Who owns this?",
                        AssistantIntent.OWNERSHIP);

        String raw = provider.complete(prompt);
        assertThat(raw).contains("evidenceIds");
        assertThat(raw).contains("11111111-1111-1111-1111-111111111111");

        List<String> tokens = new ArrayList<>();
        AtomicReference<String> done = new AtomicReference<>();
        provider.stream(prompt, tokens::add, done::set);
        assertThat(tokens).isNotEmpty();
        assertThat(done.get()).isEqualTo(raw);
        assertThat(provider.name()).contains("stub");
    }
}
