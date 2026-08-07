package com.gitdetective.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.assistant.context.EvidenceContextBuilder;
import com.gitdetective.assistant.context.EvidenceContextBuilder.EvidenceContext;
import com.gitdetective.assistant.conversation.ConversationExporter;
import com.gitdetective.assistant.conversation.SuggestedQuestionGenerator;
import com.gitdetective.assistant.formatter.AssistantResponseFormatter;
import com.gitdetective.assistant.formatter.AssistantResponseFormatter.AssistantAnswer;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.intent.IntentDetector;
import com.gitdetective.assistant.memory.AssistantConversationEntity;
import com.gitdetective.assistant.memory.AssistantConversationJpaRepository;
import com.gitdetective.assistant.memory.AssistantMessageEntity;
import com.gitdetective.assistant.memory.AssistantMessageJpaRepository;
import com.gitdetective.assistant.prompt.PromptBuilder;
import com.gitdetective.assistant.prompt.PromptBuilder.PromptPayload;
import com.gitdetective.assistant.provider.AiProvider;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator;
import com.gitdetective.assistant.validator.AssistantEvidenceValidator.ValidatedAiResponse;
import com.gitdetective.assistant.validator.AssistantValidationException;
import com.gitdetective.dto.request.AskAssistantRequest;
import com.gitdetective.dto.request.CreateAssistantConversationRequest;
import com.gitdetective.dto.response.AssistantConversationResponse;
import com.gitdetective.dto.response.AssistantConversationResponse.AssistantMessageResponse;
import com.gitdetective.evidence.EvidenceEngine;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.exception.ResourceNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Orchestrates the Phase 4 assistant pipeline. The only data source is {@link EvidenceEngine}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssistantService {

    private final EvidenceEngine evidenceEngine;
    private final IntentDetector intentDetector;
    private final EvidenceContextBuilder contextBuilder;
    private final PromptBuilder promptBuilder;
    private final AiProvider aiProvider;
    private final AssistantEvidenceValidator evidenceValidator;
    private final AssistantResponseFormatter responseFormatter;
    private final SuggestedQuestionGenerator suggestedQuestionGenerator;
    private final ConversationExporter conversationExporter;
    private final AssistantConversationJpaRepository conversationRepository;
    private final AssistantMessageJpaRepository messageRepository;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<UUID, AtomicBoolean> cancellations = new ConcurrentHashMap<>();

    @Transactional
    public AssistantConversationResponse createConversation(
            CreateAssistantConversationRequest request) {
        EvidenceBundle bundle = evidenceEngine.gather(request.investigationId());
        log.info(
                "Conversation creation investigationId={} repositoryId={}",
                bundle.investigationId(),
                bundle.repositoryId());

        AssistantConversationEntity conversation =
                AssistantConversationEntity.builder()
                        .repositoryId(bundle.repositoryId())
                        .investigationId(bundle.investigationId())
                        .title(
                                "Investigation "
                                        + bundle.investigationTarget().targetType()
                                        + " "
                                        + bundle.investigationTarget().targetRef())
                        .build();
        conversationRepository.save(conversation);

        EvidenceContext context =
                contextBuilder.build(bundle, AssistantIntent.GENERAL_INVESTIGATION);
        List<String> suggestions =
                suggestedQuestionGenerator.suggest(
                        AssistantIntent.GENERAL_INVESTIGATION, context, false);

        return toResponse(conversation, List.of(), suggestions);
    }

    @Transactional(readOnly = true)
    public List<AssistantConversationResponse> listByInvestigation(UUID investigationId) {
        return conversationRepository
                .findByInvestigationIdOrderByCreatedAtDesc(investigationId)
                .stream()
                .map(
                        c ->
                                toResponse(
                                        c,
                                        messageRepository.findByConversationIdOrderBySortOrderAsc(
                                                c.getId()),
                                        List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public AssistantConversationResponse getConversation(UUID conversationId) {
        AssistantConversationEntity conversation = requireConversation(conversationId);
        List<AssistantMessageEntity> messages =
                messageRepository.findByConversationIdOrderBySortOrderAsc(conversationId);
        EvidenceBundle bundle = evidenceEngine.gather(conversation.getInvestigationId());
        EvidenceContext context =
                contextBuilder.build(bundle, AssistantIntent.GENERAL_INVESTIGATION);
        return toResponse(
                conversation,
                messages,
                suggestedQuestionGenerator.suggest(
                        AssistantIntent.GENERAL_INVESTIGATION, context, false));
    }

    @Transactional
    public AssistantAnswer ask(UUID conversationId, AskAssistantRequest request) {
        AssistantConversationEntity conversation = requireConversation(conversationId);
        String question = promptBuilder.sanitize(request.question());
        AssistantIntent intent = intentDetector.detect(question);
        log.info("Intent detection conversationId={} intent={}", conversationId, intent);

        if (intentDetector.isUnsupported(intent, question)) {
            throw new RepositoryAnalysisException(
                    HttpStatus.BAD_REQUEST,
                    "UNSUPPORTED_QUESTION",
                    "This assistant only answers repository investigation questions. "
                            + "Code editing, commits, and autonomous actions are out of scope.");
        }

        EvidenceBundle bundle = evidenceEngine.gather(conversation.getInvestigationId());
        log.info(
                "Evidence generation conversationId={} investigationId={} items={}",
                conversationId,
                bundle.investigationId(),
                bundle.allEvidence().size());

        EvidenceContext context = contextBuilder.build(bundle, intent);
        PromptPayload prompt = promptBuilder.build(question, intent, context);
        prompt = enrichWithMemory(prompt, conversationId);
        log.info(
                "Prompt generation conversationId={} intent={} provider={}",
                conversationId,
                intent,
                aiProvider.name());

        String raw = aiProvider.complete(prompt);
        ValidatedAiResponse validated;
        try {
            validated = evidenceValidator.validate(raw, context);
            log.info(
                    "Validation success conversationId={} evidenceCited={}",
                    conversationId,
                    validated.evidenceIds().size());
        } catch (AssistantValidationException ex) {
            log.warn(
                    "Validation failure conversationId={} reason={}",
                    conversationId,
                    ex.getMessage());
            throw new RepositoryAnalysisException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "AI_RESPONSE_INVALID",
                    "AI response failed evidence validation: " + ex.getMessage(),
                    ex);
        }

        UUID assistantMessageId = UUID.randomUUID();
        AssistantAnswer answer =
                responseFormatter.format(validated, context, intent, assistantMessageId);

        persistTurn(conversation, question, intent, answer);
        return answer;
    }

    public SseEmitter askStream(UUID conversationId, AskAssistantRequest request) {
        SseEmitter emitter = new SseEmitter(180_000L);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        cancellations.put(conversationId, cancelled);
        emitter.onCompletion(() -> cancellations.remove(conversationId, cancelled));
        emitter.onTimeout(
                () -> {
                    cancelled.set(true);
                    cancellations.remove(conversationId, cancelled);
                });
        emitter.onError(
                e -> {
                    cancelled.set(true);
                    cancellations.remove(conversationId, cancelled);
                });

        Thread worker =
                new Thread(
                        () -> {
                            try {
                                runStream(conversationId, request, emitter, cancelled);
                            } catch (Exception ex) {
                                log.error(
                                        "Streaming failure conversationId={} error={}",
                                        conversationId,
                                        ex.getMessage());
                                try {
                                    emitter.send(
                                            SseEmitter.event().name("error").data(ex.getMessage()));
                                } catch (IOException ignored) {
                                    // ignore
                                }
                                emitter.completeWithError(ex);
                            }
                        },
                        "assistant-stream-" + conversationId);
        worker.start();
        return emitter;
    }

    public void cancelStream(UUID conversationId) {
        AtomicBoolean flag = cancellations.get(conversationId);
        if (flag != null) {
            flag.set(true);
            log.info("Streaming cancellation conversationId={}", conversationId);
        }
    }

    @Transactional(readOnly = true)
    public ConversationExporter.ExportResult export(UUID conversationId, String format) {
        AssistantConversationEntity conversation = requireConversation(conversationId);
        List<AssistantMessageEntity> messages =
                messageRepository.findByConversationIdOrderBySortOrderAsc(conversationId);
        return conversationExporter.export(conversation, messages, format);
    }

    @Transactional(readOnly = true)
    public List<String> suggestions(UUID conversationId) {
        AssistantConversationEntity conversation = requireConversation(conversationId);
        EvidenceBundle bundle = evidenceEngine.gather(conversation.getInvestigationId());
        EvidenceContext context =
                contextBuilder.build(bundle, AssistantIntent.GENERAL_INVESTIGATION);
        return suggestedQuestionGenerator.suggest(
                AssistantIntent.GENERAL_INVESTIGATION, context, false);
    }

    private void runStream(
            UUID conversationId,
            AskAssistantRequest request,
            SseEmitter emitter,
            AtomicBoolean cancelled)
            throws IOException {
        log.info("Streaming lifecycle start conversationId={}", conversationId);
        AssistantConversationEntity conversation = requireConversation(conversationId);
        String question = promptBuilder.sanitize(request.question());
        AssistantIntent intent = intentDetector.detect(question);
        if (intentDetector.isUnsupported(intent, question)) {
            emitter.send(
                    SseEmitter.event().name("error").data("Unsupported investigation question"));
            emitter.complete();
            return;
        }

        EvidenceBundle bundle = evidenceEngine.gather(conversation.getInvestigationId());
        EvidenceContext context = contextBuilder.build(bundle, intent);
        PromptPayload prompt =
                enrichWithMemory(promptBuilder.build(question, intent, context), conversationId);

        emitter.send(SseEmitter.event().name("intent").data(intent.name()));

        StringBuilder full = new StringBuilder();
        Consumer<String> onToken =
                token -> {
                    if (cancelled.get() || Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    full.append(token);
                    try {
                        emitter.send(SseEmitter.event().name("token").data(token));
                    } catch (IOException e) {
                        cancelled.set(true);
                        Thread.currentThread().interrupt();
                    }
                };

        aiProvider.stream(
                prompt,
                onToken,
                complete -> {
                    // completion handled below after stream returns
                });

        if (cancelled.get()) {
            emitter.send(SseEmitter.event().name("cancelled").data("cancelled"));
            emitter.complete();
            log.info("Streaming lifecycle cancelled conversationId={}", conversationId);
            return;
        }

        ValidatedAiResponse validated = evidenceValidator.validate(full.toString(), context);
        UUID assistantMessageId = UUID.randomUUID();
        AssistantAnswer answer =
                responseFormatter.format(validated, context, intent, assistantMessageId);
        persistTurn(conversation, question, intent, answer);

        emitter.send(
                SseEmitter.event().name("answer").data(objectMapper.writeValueAsString(answer)));
        emitter.send(SseEmitter.event().name("done").data("ok"));
        emitter.complete();
        log.info("Streaming lifecycle complete conversationId={}", conversationId);
    }

    private PromptPayload enrichWithMemory(PromptPayload prompt, UUID conversationId) {
        List<AssistantMessageEntity> prior =
                messageRepository.findByConversationIdOrderBySortOrderAsc(conversationId);
        if (prior.isEmpty()) {
            return prompt;
        }
        StringBuilder memory = new StringBuilder("Prior turns within this investigation:\n");
        int start = Math.max(0, prior.size() - 6);
        for (int i = start; i < prior.size(); i++) {
            AssistantMessageEntity m = prior.get(i);
            memory.append(m.getRole()).append(": ").append(m.getContent()).append('\n');
        }
        String developer = prompt.developerInstructions() + "\n" + memory;
        return new PromptPayload(
                prompt.systemPrompt(),
                developer,
                prompt.evidenceContext(),
                prompt.userQuestion(),
                prompt.intent());
    }

    private void persistTurn(
            AssistantConversationEntity conversation,
            String question,
            AssistantIntent intent,
            AssistantAnswer answer) {
        int next = (int) messageRepository.countByConversationId(conversation.getId());
        AssistantMessageEntity user =
                AssistantMessageEntity.builder()
                        .conversationId(conversation.getId())
                        .role("USER")
                        .content(question)
                        .intent(intent.name())
                        .sortOrder(next)
                        .build();
        AssistantMessageEntity assistant =
                AssistantMessageEntity.builder()
                        .id(answer.messageId())
                        .conversationId(conversation.getId())
                        .role("ASSISTANT")
                        .content(answer.answer())
                        .intent(intent.name())
                        .confidence(answer.confidence())
                        .responsePayload(conversationExporter.serializeAnswer(answer))
                        .evidenceIdsJson(writeIds(answer))
                        .sortOrder(next + 1)
                        .build();
        messageRepository.save(user);
        messageRepository.save(assistant);
        conversationRepository.save(conversation);
    }

    private String writeIds(AssistantAnswer answer) {
        try {
            return objectMapper.writeValueAsString(
                    answer.evidenceUsed().stream().map(c -> c.evidenceId().toString()).toList());
        } catch (Exception ex) {
            return "[]";
        }
    }

    private AssistantConversationEntity requireConversation(UUID id) {
        return conversationRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Assistant conversation not found: " + id));
    }

    private AssistantConversationResponse toResponse(
            AssistantConversationEntity conversation,
            List<AssistantMessageEntity> messages,
            List<String> suggestions) {
        List<AssistantMessageResponse> mapped = new ArrayList<>();
        for (AssistantMessageEntity message : messages) {
            AssistantAnswer answer = null;
            if (message.getResponsePayload() != null && !message.getResponsePayload().isBlank()) {
                try {
                    answer =
                            objectMapper.readValue(
                                    message.getResponsePayload(), AssistantAnswer.class);
                } catch (Exception ignored) {
                    answer = null;
                }
            }
            mapped.add(
                    new AssistantMessageResponse(
                            message.getId(),
                            message.getRole(),
                            message.getContent(),
                            message.getIntent(),
                            message.getConfidence(),
                            message.getCreatedAt(),
                            answer));
        }
        return new AssistantConversationResponse(
                conversation.getId(),
                conversation.getRepositoryId(),
                conversation.getInvestigationId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                mapped,
                suggestions);
    }
}
