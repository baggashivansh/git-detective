package com.gitdetective.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gitdetective.assistant.AssistantService;
import com.gitdetective.assistant.formatter.AssistantResponseFormatter.AssistantAnswer;
import com.gitdetective.assistant.formatter.AssistantResponseFormatter.SupportingArtifacts;
import com.gitdetective.dto.response.AssistantConversationResponse;
import com.gitdetective.exception.GlobalExceptionHandler;
import com.gitdetective.security.SecurityConfig;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AssistantController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AssistantControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AssistantService assistantService;

    @Test
    @DisplayName("creates conversation and asks a question")
    void createAndAsk() throws Exception {
        UUID conversationId = UUID.randomUUID();
        UUID investigationId = UUID.randomUUID();
        when(assistantService.createConversation(any()))
                .thenReturn(
                        new AssistantConversationResponse(
                                conversationId,
                                UUID.randomUUID(),
                                investigationId,
                                "title",
                                Instant.now(),
                                Instant.now(),
                                List.of(),
                                List.of("Summarize this investigation.")));

        mockMvc.perform(
                        post("/assistant/conversations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"investigationId\":\"" + investigationId + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(conversationId.toString()));

        when(assistantService.ask(eq(conversationId), any()))
                .thenReturn(
                        new AssistantAnswer(
                                UUID.randomUUID(),
                                "Ada owns the module.",
                                List.of(),
                                90,
                                new SupportingArtifacts(List.of(), List.of(), List.of(), List.of()),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of("Explain this further."),
                                "OWNERSHIP",
                                false));

        mockMvc.perform(
                        post("/assistant/conversations/" + conversationId + "/messages")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"question\":\"Who owns this module?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answer").value("Ada owns the module."))
                .andExpect(jsonPath("$.data.confidence").value(90));
    }
}
