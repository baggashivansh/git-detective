package com.gitdetective.assistant.conversation;

import com.gitdetective.assistant.context.EvidenceContextBuilder.EvidenceContext;
import com.gitdetective.assistant.intent.AssistantIntent;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** Deterministic suggested questions derived from available evidence — no AI. */
@Component
public class SuggestedQuestionGenerator {

    public List<String> suggest(
            AssistantIntent intent, EvidenceContext context, boolean insufficient) {
        List<String> suggestions = new ArrayList<>();
        if (insufficient) {
            suggestions.add("Summarize the investigation findings.");
            suggestions.add("What ownership evidence is available?");
            return suggestions;
        }

        suggestions.add("Summarize this investigation.");
        if (!context.supportingContributors().isEmpty()) {
            suggestions.add("Who owns this module?");
        }
        if (!context.supportingCommits().isEmpty()) {
            suggestions.add("What changed recently?");
        }
        if (context.compactEvidence().contains("IMPACT")
                || intent == AssistantIntent.IMPACT
                || intent == AssistantIntent.SUMMARY) {
            suggestions.add("Explain the blast radius.");
        }
        if (context.compactEvidence().contains("RELATIONSHIP")
                || intent == AssistantIntent.RELATIONSHIP) {
            suggestions.add("Explain dependency relationships.");
        }
        if (context.compactEvidence().contains("PACKAGE_HEALTH")) {
            suggestions.add("Summarize package health.");
        }
        if (context.compactEvidence().contains("HOTSPOT")) {
            suggestions.add("Why is this file risky?");
        }
        if (context.compactEvidence().contains("TRACE")
                || intent == AssistantIntent.AUTHENTICATION
                || intent == AssistantIntent.REQUEST_FLOW) {
            suggestions.add("Explain the authentication flow.");
            suggestions.add("Explain the request lifecycle.");
        }
        suggestions.add("Explain this further.");

        return suggestions.stream().distinct().limit(6).toList();
    }
}
