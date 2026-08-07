package com.gitdetective.assistant.intent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntentDetectorTest {

    private final IntentDetector detector = new IntentDetector();

    @Test
    @DisplayName("detects ownership, impact, auth, and unsupported intents")
    void detectsIntents() {
        assertThat(detector.detect("Who owns this module?")).isEqualTo(AssistantIntent.OWNERSHIP);
        assertThat(detector.detect("Explain the blast radius")).isEqualTo(AssistantIntent.IMPACT);
        assertThat(detector.detect("Explain the authentication flow"))
                .isEqualTo(AssistantIntent.AUTHENTICATION);
        assertThat(detector.detect("What changed recently?")).isEqualTo(AssistantIntent.TIMELINE);
        assertThat(detector.detect("Summarize this investigation"))
                .isEqualTo(AssistantIntent.SUMMARY);
        assertThat(detector.detect("Please generate code for a fix"))
                .isEqualTo(AssistantIntent.UNKNOWN);
        assertThat(detector.isUnsupported(AssistantIntent.UNKNOWN, "create PR")).isTrue();
    }
}
