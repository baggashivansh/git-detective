"use client";

import * as React from "react";
import { motion } from "framer-motion";
import { Download, MessageSquarePlus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { ChatComposer } from "@/components/chat/chat-composer";
import { EvidencePanel } from "@/components/evidence/evidence-panel";
import { ChatMessage } from "@/components/messages/chat-message";
import {
  useAskAssistant,
  useAssistantConversation,
  useAssistantConversations,
  useCreateAssistantConversation,
  useExportConversation,
} from "@/features/assistant/hooks/use-assistant";
import {
  askQuestion,
  askQuestionStream,
  cancelStream,
} from "@/services/assistant";
import type { EvidenceCitation } from "@/types/assistant";

interface AssistantChatViewProps {
  investigationId: string;
}

export function AssistantChatView({ investigationId }: AssistantChatViewProps) {
  const list = useAssistantConversations(investigationId);
  const createConversation = useCreateAssistantConversation(investigationId);
  const [conversationId, setConversationId] = React.useState<string | null>(null);
  const activeConversationId = conversationId ?? list.data?.[0]?.id ?? null;
  const conversation = useAssistantConversation(activeConversationId);
  const askBlocking = useAskAssistant(activeConversationId ?? "");
  const exporter = useExportConversation(activeConversationId ?? "");
  const [selectedEvidenceId, setSelectedEvidenceId] = React.useState<string | null>(
    null,
  );
  const [preferStream, setPreferStream] = React.useState(true);
  const [isStreaming, setIsStreaming] = React.useState(false);
  const [streamingText, setStreamingText] = React.useState("");
  const [streamError, setStreamError] = React.useState<string | null>(null);
  const abortRef = React.useRef<AbortController | null>(null);

  const citations: EvidenceCitation[] =
    conversation.data?.messages
      .slice()
      .reverse()
      .find((m) => m.answer?.evidenceUsed?.length)?.answer?.evidenceUsed ?? [];

  const suggestions =
    conversation.data?.suggestedQuestions ??
    conversation.data?.messages
      .slice()
      .reverse()
      .find((m) => m.answer?.suggestedFollowUpQuestions?.length)?.answer
      ?.suggestedFollowUpQuestions ??
    [];

  async function ensureConversation() {
    if (activeConversationId) return activeConversationId;
    const created = await createConversation.mutateAsync();
    setConversationId(created.id);
    return created.id;
  }

  async function handleAsk(question: string) {
    const id = await ensureConversation();
    setConversationId(id);
    setStreamError(null);

    if (preferStream) {
      setIsStreaming(true);
      setStreamingText("");
      abortRef.current = new AbortController();
      try {
        await askQuestionStream(id, question, {
          signal: abortRef.current.signal,
          onToken: (token) => setStreamingText((prev) => prev + token),
          onError: (message) => setStreamError(message),
        });
        await conversation.refetch();
      } catch (err) {
        if ((err as Error).name !== "AbortError") {
          setStreamError(err instanceof Error ? err.message : "Stream failed");
        }
      } finally {
        setIsStreaming(false);
        setStreamingText("");
        abortRef.current = null;
      }
      return;
    }

    if (activeConversationId === id) {
      await askBlocking.mutateAsync(question);
    } else {
      await askQuestion(id, question);
      await conversation.refetch();
    }
  }

  async function handleCancel() {
    abortRef.current?.abort();
    if (activeConversationId) {
      await cancelStream(activeConversationId);
    }
    setIsStreaming(false);
  }

  async function handleExport(format: "markdown" | "json" | "html") {
    if (!activeConversationId) return;
    const result = await exporter.mutateAsync(format);
    const blob = new Blob([result.content], {
      type:
        format === "json"
          ? "application/json"
          : format === "html"
            ? "text/html"
            : "text/markdown",
    });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = `assistant-${activeConversationId}.${format === "markdown" ? "md" : format}`;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  return (
    <div className="grid min-h-[70vh] gap-4 lg:grid-cols-[minmax(0,1fr)_280px]">
      <div className="flex min-h-[70vh] flex-col overflow-hidden rounded-2xl border border-border/60 bg-gradient-to-b from-card/80 to-background">
        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border/60 px-4 py-3">
          <div>
            <h3 className="text-sm font-medium">Investigation Assistant</h3>
            <p className="text-xs text-muted-foreground">
              Answers use Evidence Engine bundles only — never raw Git or parsers.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() =>
                void createConversation.mutateAsync().then((c) => setConversationId(c.id))
              }
            >
              <MessageSquarePlus className="size-4" />
              New chat
            </Button>
            <Button
              size="sm"
              variant="ghost"
              disabled={!activeConversationId}
              onClick={() => void handleExport("markdown")}
            >
              <Download className="size-4" />
              Export
            </Button>
            <Button
              size="sm"
              variant={preferStream ? "secondary" : "ghost"}
              onClick={() => setPreferStream((v) => !v)}
            >
              {preferStream ? "Streaming on" : "Streaming off"}
            </Button>
          </div>
        </div>

        <div className="flex flex-1 flex-col gap-4 overflow-y-auto p-4">
          {!conversation.data?.messages.length ? (
            <motion.div
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              className="m-auto max-w-md text-center text-sm text-muted-foreground"
            >
              Ask about ownership, timeline, impact, package health, request flows,
              or investigation findings. Unsupported actions (code edits, commits) are
              rejected.
            </motion.div>
          ) : (
            conversation.data.messages.map((message) => (
              <ChatMessage
                key={message.id}
                message={message}
                onSelectEvidence={setSelectedEvidenceId}
              />
            ))
          )}

          {isStreaming ? (
            <div
              className="max-w-[85%] rounded-2xl border border-border/60 bg-card/60 px-4 py-3 text-sm text-muted-foreground"
              role="status"
              aria-live="polite"
              aria-busy="true"
            >
              <span className="mb-2 inline-flex items-center gap-2 text-[11px] uppercase tracking-wide">
                Assistant
                <span className="inline-flex gap-1">
                  <span className="size-1 animate-pulse rounded-full bg-sky-400" />
                  <span className="size-1 animate-pulse rounded-full bg-sky-400 [animation-delay:120ms]" />
                  <span className="size-1 animate-pulse rounded-full bg-sky-400 [animation-delay:240ms]" />
                </span>
              </span>
              <pre className="whitespace-pre-wrap font-sans text-foreground/90">
                {streamingText || "Thinking with evidence…"}
              </pre>
            </div>
          ) : null}

          {streamError || askBlocking.error ? (
            <p className="text-sm text-destructive">
              {streamError ??
                (askBlocking.error instanceof Error
                  ? askBlocking.error.message
                  : "Request failed")}
            </p>
          ) : null}
        </div>

        <ChatComposer
          disabled={createConversation.isPending || askBlocking.isPending}
          isStreaming={isStreaming}
          suggestions={suggestions}
          onSubmit={(q) => void handleAsk(q)}
          onCancel={() => void handleCancel()}
        />
      </div>

      <EvidencePanel
        citations={citations}
        selectedId={selectedEvidenceId}
        onSelect={setSelectedEvidenceId}
      />
    </div>
  );
}
