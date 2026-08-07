import { clientEnv } from "@/lib/env";
import { apiGet, apiPost } from "@/services/investigation/api-client";
import type {
  AssistantAnswer,
  AssistantConversation,
  ConversationExport,
} from "@/types/assistant";

const API_BASE = clientEnv.NEXT_PUBLIC_API_BASE_URL;

export function createConversation(investigationId: string) {
  return apiPost<AssistantConversation>("/assistant/conversations", {
    investigationId,
  });
}

export function listConversations(investigationId: string) {
  return apiGet<AssistantConversation[]>(
    `/assistant/conversations?investigationId=${encodeURIComponent(investigationId)}`,
  );
}

export function getConversation(conversationId: string) {
  return apiGet<AssistantConversation>(
    `/assistant/conversations/${conversationId}`,
  );
}

export function askQuestion(conversationId: string, question: string) {
  return apiPost<AssistantAnswer>(
    `/assistant/conversations/${conversationId}/messages`,
    { question },
  );
}

export function getSuggestions(conversationId: string) {
  return apiGet<string[]>(
    `/assistant/conversations/${conversationId}/suggestions`,
  );
}

export function exportConversation(
  conversationId: string,
  format: "markdown" | "json" | "html",
) {
  return apiGet<ConversationExport>(
    `/assistant/conversations/${conversationId}/export?format=${format}`,
  );
}

export function cancelStream(conversationId: string) {
  return apiPost<string>(
    `/assistant/conversations/${conversationId}/cancel`,
    {},
  );
}

export type StreamHandlers = {
  onIntent?: (intent: string) => void;
  onToken?: (token: string) => void;
  onAnswer?: (answer: AssistantAnswer) => void;
  onError?: (message: string) => void;
  onDone?: () => void;
  onCancelled?: () => void;
  signal?: AbortSignal;
};

/** POST SSE stream using fetch + ReadableStream (EventSource is GET-only). */
export async function askQuestionStream(
  conversationId: string,
  question: string,
  handlers: StreamHandlers,
): Promise<void> {
  const response = await fetch(
    `${API_BASE}/assistant/conversations/${conversationId}/messages/stream`,
    {
      method: "POST",
      headers: {
        Accept: "text/event-stream",
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ question }),
      signal: handlers.signal,
    },
  );

  if (!response.ok || !response.body) {
    throw new Error(`Stream failed (${response.status})`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const parts = buffer.split("\n\n");
    buffer = parts.pop() ?? "";

    for (const part of parts) {
      const lines = part.split("\n");
      let event = "message";
      let data = "";
      for (const line of lines) {
        if (line.startsWith("event:")) {
          event = line.slice(6).trim();
        } else if (line.startsWith("data:")) {
          data += line.slice(5).trim();
        }
      }
      if (!data) continue;
      if (event === "intent") handlers.onIntent?.(data);
      else if (event === "token") handlers.onToken?.(data);
      else if (event === "answer") {
        handlers.onAnswer?.(JSON.parse(data) as AssistantAnswer);
      } else if (event === "error") handlers.onError?.(data);
      else if (event === "cancelled") handlers.onCancelled?.();
      else if (event === "done") handlers.onDone?.();
    }
  }
}
