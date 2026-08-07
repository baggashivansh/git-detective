"use client";

import * as React from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  askQuestion,
  askQuestionStream,
  cancelStream,
  createConversation,
  exportConversation,
  getConversation,
  listConversations,
} from "@/services/assistant";
import type { AssistantAnswer } from "@/types/assistant";

export const assistantKeys = {
  all: ["assistant"] as const,
  list: (investigationId: string) =>
    [...assistantKeys.all, "list", investigationId] as const,
  detail: (conversationId: string) =>
    [...assistantKeys.all, "detail", conversationId] as const,
};

export function useAssistantConversations(investigationId: string) {
  return useQuery({
    queryKey: assistantKeys.list(investigationId),
    queryFn: () => listConversations(investigationId),
    enabled: Boolean(investigationId),
  });
}

export function useAssistantConversation(conversationId: string | null) {
  return useQuery({
    queryKey: assistantKeys.detail(conversationId ?? "none"),
    queryFn: () => getConversation(conversationId!),
    enabled: Boolean(conversationId),
  });
}

export function useCreateAssistantConversation(investigationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => createConversation(investigationId),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: assistantKeys.list(investigationId),
      });
    },
  });
}

export function useAskAssistant(conversationId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (question: string) => askQuestion(conversationId, question),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: assistantKeys.detail(conversationId),
      });
    },
  });
}

export function useExportConversation(conversationId: string) {
  return useMutation({
    mutationFn: (format: "markdown" | "json" | "html") =>
      exportConversation(conversationId, format),
  });
}

export function useStreamingAsk(conversationId: string) {
  const queryClient = useQueryClient();
  const [streamingText, setStreamingText] = React.useState("");
  const [isStreaming, setIsStreaming] = React.useState(false);
  const [error, setError] = React.useState<string | null>(null);
  const abortRef = React.useRef<AbortController | null>(null);

  const start = React.useCallback(
    async (question: string): Promise<AssistantAnswer | null> => {
      setIsStreaming(true);
      setStreamingText("");
      setError(null);
      abortRef.current = new AbortController();
      let finalAnswer: AssistantAnswer | null = null;

      try {
        await askQuestionStream(conversationId, question, {
          signal: abortRef.current.signal,
          onToken: (token) => setStreamingText((prev) => prev + token),
          onAnswer: (answer) => {
            finalAnswer = answer;
          },
          onError: (message) => setError(message),
        });
        void queryClient.invalidateQueries({
          queryKey: assistantKeys.detail(conversationId),
        });
        return finalAnswer;
      } catch (err) {
        if ((err as Error).name !== "AbortError") {
          setError(err instanceof Error ? err.message : "Stream failed");
        }
        return null;
      } finally {
        setIsStreaming(false);
        abortRef.current = null;
      }
    },
    [conversationId, queryClient],
  );

  const cancel = React.useCallback(async () => {
    abortRef.current?.abort();
    await cancelStream(conversationId);
    setIsStreaming(false);
  }, [conversationId]);

  return { start, cancel, streamingText, isStreaming, error };
}
