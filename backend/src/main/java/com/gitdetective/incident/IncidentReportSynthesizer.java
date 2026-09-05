package com.gitdetective.incident;

import com.gitdetective.assistant.context.EvidenceContextBuilder;
import com.gitdetective.assistant.context.EvidenceContextBuilder.EvidenceContext;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.intent.IntentDetector;
import com.gitdetective.assistant.prompt.PromptBuilder;
import com.gitdetective.assistant.prompt.PromptBuilder.PromptPayload;
import com.gitdetective.assistant.provider.AiProvider;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator.ValidatedAiResponse;
import com.gitdetective.assistant.validator.AssistantValidationException;
import com.gitdetective.dto.response.IncidentReportResponse;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.evidence.EvidenceEngine;
import com.gitdetective.evidence.model.EvidenceBundle;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Gathers the Evidence Bundle, optionally asks the AI provider, validates citations, then compiles
 * the structured report. Invalid or hallucinated AI output is discarded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentReportSynthesizer {

    private final EvidenceEngine evidenceEngine;
    private final IntentDetector intentDetector;
    private final EvidenceContextBuilder contextBuilder;
    private final PromptBuilder promptBuilder;
    private final AiProvider aiProvider;
    private final AssistantEvidenceValidator evidenceValidator;
    private final IncidentReportCompiler compiler;

    public IncidentReportResponse synthesize(UUID investigationId, String question) {
        EvidenceBundle bundle = evidenceEngine.gather(investigationId);
        return synthesize(investigationId, question, bundle, null);
    }

    public IncidentReportResponse synthesize(
            UUID investigationId,
            String question,
            EvidenceBundle bundle,
            InvestigationDetailResponse detail) {
        EvidenceBundle evidence = bundle == null ? evidenceEngine.gather(investigationId) : bundle;
        InvestigationDetailResponse slices = detail;
        if (slices == null) {
            throw new IllegalArgumentException("Investigation detail is required for compilation");
        }

        ValidatedAiResponse validated = null;
        if (!evidence.allEvidence().isEmpty()) {
            AssistantIntent intent = intentDetector.detect(question);
            EvidenceContext context = contextBuilder.build(evidence, intent);
            PromptPayload prompt = promptBuilder.buildIncident(question, intent, context);
            try {
                String raw = aiProvider.complete(prompt);
                validated = evidenceValidator.validate(raw, context);
                log.info(
                        "Incident AI validation success investigationId={} cited={}",
                        investigationId,
                        validated.evidenceIds().size());
            } catch (AssistantValidationException ex) {
                log.warn(
                        "Incident AI output rejected investigationId={} reason={}",
                        investigationId,
                        ex.getMessage());
                validated = null;
            } catch (RuntimeException ex) {
                log.warn(
                        "Incident AI provider failed investigationId={} reason={}",
                        investigationId,
                        ex.getMessage());
                validated = null;
            }
        }

        return compiler.compile(question, slices, evidence, validated);
    }
}
