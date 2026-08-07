package com.gitdetective.assistant.provider;

import com.gitdetective.assistant.prompt.PromptBuilder.PromptPayload;
import java.util.function.Consumer;

/**
 * Replaceable AI provider abstraction. Application code depends only on this interface — never on a
 * vendor SDK.
 */
public interface AiProvider {

    /** Blocking completion returning raw model text. */
    String complete(PromptPayload prompt);

    /**
     * Streams partial tokens. Invokes {@code onToken} for each chunk, then {@code onComplete} with
     * the full text. Implementations must honor interruption via Thread interrupt / cancel flag.
     */
    void stream(PromptPayload prompt, Consumer<String> onToken, Consumer<String> onComplete);

    /** Provider name for logging (never include secrets). */
    String name();
}
