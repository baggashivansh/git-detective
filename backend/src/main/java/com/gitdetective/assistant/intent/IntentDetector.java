package com.gitdetective.assistant.intent;

import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/** Keyword / pattern based intent detection. Deterministic — does not call an AI provider. */
@Component
public class IntentDetector {

    private static final Pattern UNSUPPORTED =
            Pattern.compile(
                    "(?i)\\b(write\\s+code|generate\\s+code|create\\s+pr|pull\\s+request|"
                            + "commit\\s+this|fix\\s+the\\s+bug|refactor\\s+this|deploy|delete\\s+file)\\b");

    public AssistantIntent detect(String question) {
        if (question == null || question.isBlank()) {
            return AssistantIntent.UNKNOWN;
        }
        String q = question.toLowerCase(Locale.ROOT).trim();

        if (UNSUPPORTED.matcher(q).find()) {
            return AssistantIntent.UNKNOWN;
        }

        if (containsAny(q, "auth", "security config", "jwt", "login", "userdetailsservice")) {
            return AssistantIntent.AUTHENTICATION;
        }
        if (containsAny(q, "request flow", "request lifecycle", "controller", "service layer")) {
            return AssistantIntent.REQUEST_FLOW;
        }
        if (containsAny(q, "own", "who owns", "contributor", "bus factor")) {
            return AssistantIntent.OWNERSHIP;
        }
        if (containsAny(q, "timeline", "recent", "what changed", "evolution", "history")) {
            return AssistantIntent.TIMELINE;
        }
        if (containsAny(q, "blast radius", "impact", "affected", "what breaks")) {
            return AssistantIntent.IMPACT;
        }
        if (containsAny(q, "depend", "relationship", "imports", "extends", "implements")) {
            return AssistantIntent.RELATIONSHIP;
        }
        if (containsAny(q, "architecture", "structure", "how is it organized")) {
            return AssistantIntent.ARCHITECTURE;
        }
        if (containsAny(q, "package health", "risk level", "complexity")) {
            return AssistantIntent.PACKAGE_HEALTH;
        }
        if (containsAny(q, "hotspot", "risky file", "frequently modified", "large class")) {
            return AssistantIntent.HOTSPOT;
        }
        if (containsAny(q, "statistic", "stats", "how many", "count")) {
            return AssistantIntent.STATISTICS;
        }
        if (containsAny(q, "summarize", "summary", "explain this investigation", "findings")) {
            return AssistantIntent.SUMMARY;
        }
        if (containsAny(q, "explain", "why", "what is", "describe", "tell me about")) {
            return AssistantIntent.GENERAL_INVESTIGATION;
        }
        return AssistantIntent.GENERAL_INVESTIGATION;
    }

    public boolean isUnsupported(AssistantIntent intent, String question) {
        if (intent == AssistantIntent.UNKNOWN) {
            return true;
        }
        return question != null && UNSUPPORTED.matcher(question).find();
    }

    private static boolean containsAny(String q, String... needles) {
        for (String needle : needles) {
            if (q.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
