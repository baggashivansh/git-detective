"use client";

import * as React from "react";
import { LoaderCircle, Send, Square } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

interface ChatComposerProps {
  disabled?: boolean;
  isStreaming?: boolean;
  suggestions?: string[];
  onSubmit: (question: string) => void;
  onCancel?: () => void;
}

export function ChatComposer({
  disabled,
  isStreaming,
  suggestions = [],
  onSubmit,
  onCancel,
}: ChatComposerProps) {
  const [value, setValue] = React.useState("");

  return (
    <div className="space-y-3 border-t border-border/60 bg-background/80 p-4 backdrop-blur">
      {suggestions.length ? (
        <div className="flex flex-wrap gap-2">
          {suggestions.slice(0, 4).map((suggestion) => (
            <button
              key={suggestion}
              type="button"
              disabled={disabled || isStreaming}
              onClick={() => onSubmit(suggestion)}
              className="rounded-full border border-border/60 px-3 py-1 text-xs text-muted-foreground transition hover:border-sky-500/40 hover:text-foreground disabled:opacity-40"
            >
              {suggestion}
            </button>
          ))}
        </div>
      ) : null}
      <form
        className="flex items-center gap-2"
        onSubmit={(event) => {
          event.preventDefault();
          const next = value.trim();
          if (!next || disabled || isStreaming) return;
          onSubmit(next);
          setValue("");
        }}
      >
        <Input
          value={value}
          onChange={(event) => setValue(event.target.value)}
          placeholder="Ask an investigation question…"
          disabled={disabled || isStreaming}
          className="h-11"
        />
        {isStreaming ? (
          <Button type="button" variant="secondary" onClick={onCancel}>
            <Square className="size-4" />
            Stop
          </Button>
        ) : (
          <Button type="submit" disabled={disabled || !value.trim()}>
            {disabled ? (
              <LoaderCircle className="size-4 animate-spin" />
            ) : (
              <Send className="size-4" />
            )}
            Ask
          </Button>
        )}
      </form>
    </div>
  );
}
