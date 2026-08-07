"use client";

import { Copy } from "lucide-react";
import { Button } from "@/components/ui/button";
import { MarkdownMessage } from "@/components/messages/markdown-message";
import type { AssistantMessage } from "@/types/assistant";
import { cn } from "@/lib/utils";

interface ChatMessageProps {
  message: AssistantMessage;
  onSelectEvidence?: (evidenceId: string) => void;
}

export function ChatMessage({ message, onSelectEvidence }: ChatMessageProps) {
  const isUser = message.role === "USER";

  return (
    <div
      className={cn(
        "flex w-full",
        isUser ? "justify-end" : "justify-start",
      )}
    >
      <div
        className={cn(
          "max-w-[85%] rounded-2xl border px-4 py-3",
          isUser
            ? "border-sky-500/30 bg-sky-500/10"
            : "border-border/60 bg-card/80",
        )}
      >
        <div className="mb-1 flex items-center justify-between gap-3">
          <span className="text-[11px] uppercase tracking-wide text-muted-foreground">
            {isUser ? "You" : "Assistant"}
            {message.intent ? ` · ${message.intent}` : ""}
            {message.confidence != null ? ` · ${message.confidence}%` : ""}
          </span>
          {!isUser ? (
            <Button
              variant="ghost"
              size="icon"
              className="size-7"
              aria-label="Copy response"
              onClick={() => void navigator.clipboard.writeText(message.content)}
            >
              <Copy className="size-3.5" />
            </Button>
          ) : null}
        </div>
        {isUser ? (
          <p className="text-sm leading-relaxed">{message.content}</p>
        ) : (
          <MarkdownMessage content={message.content} />
        )}
        {message.answer?.evidenceUsed?.length ? (
          <div className="mt-3 flex flex-wrap gap-1.5 border-t border-border/50 pt-3">
            {message.answer.evidenceUsed.map((citation) => (
              <button
                key={citation.evidenceId}
                type="button"
                onClick={() => onSelectEvidence?.(citation.evidenceId)}
                className="rounded-md border border-border/60 bg-background/60 px-2 py-1 text-[11px] text-muted-foreground transition hover:border-sky-500/40 hover:text-foreground"
              >
                {citation.evidenceType} · {citation.confidence}%
              </button>
            ))}
          </div>
        ) : null}
      </div>
    </div>
  );
}
