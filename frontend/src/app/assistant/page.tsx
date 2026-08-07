import { AppShell } from "@/components/layout/app-shell";
import { AssistantChatView } from "@/features/assistant/assistant-chat-view";

interface AssistantPageProps {
  searchParams: Promise<{ investigationId?: string }>;
}

export default async function AssistantPage({ searchParams }: AssistantPageProps) {
  const params = await searchParams;
  const investigationId = params.investigationId;

  return (
    <AppShell title="Assistant">
      {investigationId ? (
        <AssistantChatView investigationId={investigationId} />
      ) : (
        <div className="mx-auto max-w-xl rounded-2xl border border-border/60 bg-card/50 p-8 text-center">
          <h2 className="text-xl font-semibold">Investigation Assistant</h2>
          <p className="mt-2 text-sm text-muted-foreground">
            Open an investigation and start the assistant from its dashboard to begin
            an evidence-backed conversation.
          </p>
        </div>
      )}
    </AppShell>
  );
}
