package com.gitdetective.controller;

import com.gitdetective.assistant.AssistantService;
import com.gitdetective.assistant.conversation.ConversationExporter;
import com.gitdetective.assistant.formatter.AssistantResponseFormatter.AssistantAnswer;
import com.gitdetective.common.ApiResponse;
import com.gitdetective.dto.request.AskAssistantRequest;
import com.gitdetective.dto.request.CreateAssistantConversationRequest;
import com.gitdetective.dto.response.AssistantConversationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/assistant")
@RequiredArgsConstructor
@Tag(name = "Assistant", description = "Evidence-backed intelligent investigation assistant")
public class AssistantController {

    private final AssistantService assistantService;

    @PostMapping("/conversations")
    @Operation(summary = "Create an investigation-scoped assistant conversation")
    public ResponseEntity<ApiResponse<AssistantConversationResponse>> create(
            @Valid @RequestBody CreateAssistantConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(assistantService.createConversation(request)));
    }

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<AssistantConversationResponse>>> list(
            @RequestParam UUID investigationId) {
        return ResponseEntity.ok(
                ApiResponse.ok(assistantService.listByInvestigation(investigationId)));
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<ApiResponse<AssistantConversationResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assistantService.getConversation(id)));
    }

    @PostMapping("/conversations/{id}/messages")
    @Operation(summary = "Ask an investigation question (blocking)")
    public ResponseEntity<ApiResponse<AssistantAnswer>> ask(
            @PathVariable UUID id, @Valid @RequestBody AskAssistantRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(assistantService.ask(id, request)));
    }

    @PostMapping(
            value = "/conversations/{id}/messages/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Ask an investigation question with SSE streaming")
    public SseEmitter askStream(
            @PathVariable UUID id, @Valid @RequestBody AskAssistantRequest request) {
        return assistantService.askStream(id, request);
    }

    @PostMapping("/conversations/{id}/cancel")
    public ResponseEntity<ApiResponse<String>> cancel(@PathVariable UUID id) {
        assistantService.cancelStream(id);
        return ResponseEntity.ok(ApiResponse.ok("cancelled"));
    }

    @GetMapping("/conversations/{id}/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> suggestions(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(assistantService.suggestions(id)));
    }

    @GetMapping("/conversations/{id}/export")
    public ResponseEntity<ApiResponse<ConversationExporter.ExportResult>> export(
            @PathVariable UUID id, @RequestParam(defaultValue = "markdown") String format) {
        return ResponseEntity.ok(ApiResponse.ok(assistantService.export(id, format)));
    }
}
