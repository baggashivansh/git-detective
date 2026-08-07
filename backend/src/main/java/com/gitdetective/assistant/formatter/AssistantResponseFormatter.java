package com.gitdetective.assistant.formatter;

import com.gitdetective.assistant.context.EvidenceContextBuilder.EvidenceContext;
import com.gitdetective.assistant.conversation.SuggestedQuestionGenerator;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator.ValidatedAiResponse;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Formats validated AI output into the client-facing assistant response contract. */
@Component
@RequiredArgsConstructor
public class AssistantResponseFormatter {

    private final SuggestedQuestionGenerator suggestedQuestionGenerator;

    public AssistantAnswer format(
            ValidatedAiResponse validated,
            EvidenceContext context,
            AssistantIntent intent,
            UUID messageId) {
        Map<UUID, EvidenceRecord> byId =
                context.selectedEvidence().stream()
                        .collect(
                                Collectors.toMap(
                                        EvidenceRecord::evidenceId,
                                        Function.identity(),
                                        (a, b) -> a));

        List<EvidenceCitation> citations = new ArrayList<>();
        for (UUID id : validated.evidenceIds()) {
            EvidenceRecord record = byId.get(id);
            if (record != null) {
                citations.add(
                        new EvidenceCitation(
                                record.evidenceId(),
                                record.evidenceType().name(),
                                record.source().name(),
                                record.sourceIdentifier(),
                                record.confidence(),
                                record.description()));
            }
        }

        List<String> followUps =
                suggestedQuestionGenerator.suggest(
                        intent, context, validated.insufficientEvidence());

        return new AssistantAnswer(
                messageId,
                validated.answer(),
                citations,
                validated.confidence(),
                new SupportingArtifacts(
                        validated.referencedFiles(),
                        validated.referencedCommits(),
                        validated.referencedContributors(),
                        validated.referencedPackages()),
                validated.referencedFiles(),
                validated.referencedCommits(),
                validated.referencedContributors(),
                validated.referencedPackages(),
                followUps,
                intent.name(),
                validated.insufficientEvidence());
    }

    public record EvidenceCitation(
            UUID evidenceId,
            String evidenceType,
            String provenance,
            String sourceIdentifier,
            int confidence,
            String description) {}

    public record SupportingArtifacts(
            List<String> files,
            List<String> commits,
            List<String> contributors,
            List<String> packages) {}

    public record AssistantAnswer(
            UUID messageId,
            String answer,
            List<EvidenceCitation> evidenceUsed,
            int confidence,
            SupportingArtifacts supportingArtifacts,
            List<String> referencedFiles,
            List<String> referencedCommits,
            List<String> referencedContributors,
            List<String> referencedPackages,
            List<String> suggestedFollowUpQuestions,
            String intent,
            boolean insufficientEvidence) {}
}
