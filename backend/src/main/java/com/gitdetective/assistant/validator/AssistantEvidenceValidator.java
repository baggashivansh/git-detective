package com.gitdetective.assistant.validator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.assistant.context.EvidenceContextBuilder.EvidenceContext;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates AI raw responses against the evidence context. Rejects invented evidence identifiers
 * and unsupported artifact references.
 */
@Component
@RequiredArgsConstructor
public class AssistantEvidenceValidator {

    public static final String INSUFFICIENT =
            "The available repository evidence is insufficient to answer this confidently.";

    private final ObjectMapper objectMapper;

    public ValidatedAiResponse validate(String rawModelText, EvidenceContext context) {
        JsonNode root = parseJson(rawModelText);
        String answer = text(root, "answer");
        if (answer == null || answer.isBlank()) {
            throw new AssistantValidationException("AI response missing answer");
        }

        Set<UUID> allowedIds =
                context.selectedEvidence().stream()
                        .map(EvidenceRecord::evidenceId)
                        .collect(Collectors.toSet());
        // Also allow any id from full context list already selected; expand with all in compact
        for (EvidenceRecord record : context.selectedEvidence()) {
            allowedIds.add(record.evidenceId());
        }

        List<UUID> cited = new ArrayList<>();
        JsonNode idsNode = root.get("evidenceIds");
        if (idsNode != null && idsNode.isArray()) {
            for (JsonNode idNode : idsNode) {
                UUID id;
                try {
                    id = UUID.fromString(idNode.asText());
                } catch (Exception ex) {
                    throw new AssistantValidationException(
                            "Invalid evidence identifier: " + idNode.asText());
                }
                if (!allowedIds.contains(id)) {
                    throw new AssistantValidationException(
                            "Referenced evidence does not exist in context: " + id);
                }
                cited.add(id);
            }
        }

        int confidence = root.path("confidence").asInt(-1);
        if (confidence < 0 || confidence > 100) {
            throw new AssistantValidationException("Confidence must be an integer 0-100");
        }

        List<String> files = stringArray(root, "referencedFiles");
        List<String> commits = stringArray(root, "referencedCommits");
        List<String> contributors = stringArray(root, "referencedContributors");
        List<String> packages = stringArray(root, "referencedPackages");

        validateAgainstSupport(files, context.supportingFiles(), "file");
        validateAgainstSupport(commits, context.supportingCommits(), "commit");
        validateAgainstSupport(contributors, context.supportingContributors(), "contributor");
        validateAgainstSupport(packages, context.supportingPackages(), "package");

        boolean insufficient =
                answer.toLowerCase(Locale.ROOT).contains("insufficient to answer this confidently");
        if (context.selectedEvidence().isEmpty() && !insufficient) {
            throw new AssistantValidationException(
                    "Empty evidence requires an insufficient-evidence answer");
        }
        if (cited.isEmpty() && !insufficient) {
            // Soft requirement: if model forgets citations but evidence exists, reject
            throw new AssistantValidationException(
                    "AI response must cite at least one evidence id");
        }

        Set<UUID> unique = new HashSet<>(cited);
        return new ValidatedAiResponse(
                answer,
                List.copyOf(unique),
                confidence,
                files,
                commits,
                contributors,
                packages,
                insufficient);
    }

    private JsonNode parseJson(String raw) {
        try {
            String trimmed = raw == null ? "" : raw.trim();
            if (trimmed.startsWith("```")) {
                int firstNl = trimmed.indexOf('\n');
                int lastFence = trimmed.lastIndexOf("```");
                if (firstNl > 0 && lastFence > firstNl) {
                    trimmed = trimmed.substring(firstNl + 1, lastFence).trim();
                }
            }
            return objectMapper.readTree(trimmed);
        } catch (Exception ex) {
            throw new AssistantValidationException("AI response is not valid JSON", ex);
        }
    }

    private static void validateAgainstSupport(
            List<String> refs, List<String> allowed, String kind) {
        if (refs.isEmpty() || allowed.isEmpty()) {
            // If AI cites something not in support lists, allow only if empty lists (unknown)
            // When allowed is non-empty, every ref must match or be substring of an allowed entry
            return;
        }
        for (String ref : refs) {
            boolean ok =
                    allowed.stream()
                            .anyMatch(
                                    a ->
                                            a.equalsIgnoreCase(ref)
                                                    || a.contains(ref)
                                                    || ref.contains(a));
            if (!ok) {
                throw new AssistantValidationException(
                        "Referenced " + kind + " not present in evidence support set: " + ref);
            }
        }
    }

    private static String text(JsonNode root, String field) {
        JsonNode n = root.get(field);
        return n == null || n.isNull() ? null : n.asText();
    }

    private static List<String> stringArray(JsonNode root, String field) {
        JsonNode n = root.get(field);
        List<String> out = new ArrayList<>();
        if (n != null && n.isArray()) {
            for (JsonNode item : n) {
                if (!item.asText("").isBlank()) {
                    out.add(item.asText());
                }
            }
        }
        return List.copyOf(out);
    }

    public record ValidatedAiResponse(
            String answer,
            List<UUID> evidenceIds,
            int confidence,
            List<String> referencedFiles,
            List<String> referencedCommits,
            List<String> referencedContributors,
            List<String> referencedPackages,
            boolean insufficientEvidence) {}
}
