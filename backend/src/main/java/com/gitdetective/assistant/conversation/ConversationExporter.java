package com.gitdetective.assistant.conversation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.assistant.formatter.AssistantResponseFormatter.AssistantAnswer;
import com.gitdetective.assistant.memory.AssistantConversationEntity;
import com.gitdetective.assistant.memory.AssistantMessageEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Exports conversation transcripts as Markdown, JSON, or PDF-ready HTML (no PDF binary). */
@Component
@RequiredArgsConstructor
public class ConversationExporter {

    private final ObjectMapper objectMapper;

    public ExportResult export(
            AssistantConversationEntity conversation,
            List<AssistantMessageEntity> messages,
            String format) {
        String normalized = format == null ? "markdown" : format.trim().toLowerCase();
        return switch (normalized) {
            case "json" -> new ExportResult("json", toJson(conversation, messages));
            case "html" -> new ExportResult("html", toHtml(conversation, messages));
            default -> new ExportResult("markdown", toMarkdown(conversation, messages));
        };
    }

    private String toMarkdown(
            AssistantConversationEntity conversation, List<AssistantMessageEntity> messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Assistant Conversation\n\n");
        sb.append("- Conversation: `").append(conversation.getId()).append("`\n");
        sb.append("- Investigation: `").append(conversation.getInvestigationId()).append("`\n");
        sb.append("- Repository: `").append(conversation.getRepositoryId()).append("`\n\n");
        for (AssistantMessageEntity message : messages) {
            sb.append("## ").append(message.getRole()).append("\n\n");
            sb.append(message.getContent()).append("\n\n");
            if (message.getConfidence() != null) {
                sb.append("_Confidence: ").append(message.getConfidence()).append("%_\n\n");
            }
        }
        return sb.toString();
    }

    private String toHtml(
            AssistantConversationEntity conversation, List<AssistantMessageEntity> messages) {
        String md = toMarkdown(conversation, messages);
        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><title>Assistant Conversation</title>"
                + "<style>body{font-family:ui-sans-serif,system-ui;background:#0b0f14;color:#e8eef5;padding:2rem;}"
                + "pre{white-space:pre-wrap;}</style></head><body><pre>"
                + escape(md)
                + "</pre></body></html>";
    }

    private String toJson(
            AssistantConversationEntity conversation, List<AssistantMessageEntity> messages) {
        try {
            return objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(
                            new ConversationExportView(
                                    conversation.getId().toString(),
                                    conversation.getRepositoryId().toString(),
                                    conversation.getInvestigationId().toString(),
                                    conversation.getTitle(),
                                    messages.stream()
                                            .map(
                                                    m ->
                                                            new MessageExportView(
                                                                    m.getId().toString(),
                                                                    m.getRole(),
                                                                    m.getContent(),
                                                                    m.getIntent(),
                                                                    m.getConfidence(),
                                                                    m.getResponsePayload()))
                                            .toList()));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to export conversation JSON", ex);
        }
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public record ExportResult(String format, String content) {}

    public record ConversationExportView(
            String conversationId,
            String repositoryId,
            String investigationId,
            String title,
            List<MessageExportView> messages) {}

    public record MessageExportView(
            String id,
            String role,
            String content,
            String intent,
            Integer confidence,
            String responsePayload) {}

    public String serializeAnswer(AssistantAnswer answer) {
        try {
            return objectMapper.writeValueAsString(answer);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize assistant answer", ex);
        }
    }
}
