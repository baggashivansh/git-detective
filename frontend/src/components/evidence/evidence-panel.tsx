"use client";

import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import type { EvidenceCitation } from "@/types/assistant";

interface EvidencePanelProps {
  citations: EvidenceCitation[];
  selectedId?: string | null;
  onSelect?: (id: string) => void;
}

export function EvidencePanel({
  citations,
  selectedId,
  onSelect,
}: EvidencePanelProps) {
  if (!citations.length) {
    return (
      <div className="rounded-xl border border-border/60 bg-card/40 p-4 text-sm text-muted-foreground">
        Evidence citations from the latest answer will appear here.
      </div>
    );
  }

  return (
    <ScrollArea className="h-full max-h-[70vh] rounded-xl border border-border/60 bg-card/40">
      <div className="flex flex-col gap-2 p-3">
        <p className="px-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
          Evidence used
        </p>
        {citations.map((citation) => {
          const active = citation.evidenceId === selectedId;
          return (
            <Button
              key={citation.evidenceId}
              variant={active ? "secondary" : "ghost"}
              className="h-auto w-full flex-col items-start gap-1 whitespace-normal px-3 py-2 text-left"
              onClick={() => onSelect?.(citation.evidenceId)}
            >
              <span className="text-xs font-medium text-foreground">
                {citation.evidenceType} · {citation.provenance}
              </span>
              <span className="text-[11px] text-muted-foreground">
                {citation.sourceIdentifier} · confidence {citation.confidence}%
              </span>
              <span className="text-xs text-muted-foreground">
                {citation.description}
              </span>
            </Button>
          );
        })}
      </div>
    </ScrollArea>
  );
}
