import { AppShell } from "@/components/layout/app-shell";

export default function DashboardPage() {
  return (
    <AppShell title="Overview">
      <div className="mx-auto flex max-w-3xl flex-col items-start gap-3">
        <h2 className="text-2xl font-semibold tracking-tight">Workspace</h2>
        <p className="text-muted-foreground leading-relaxed">
          The investigation workspace is ready. Repository analysis, AI, and
          evidence tools are intentionally unavailable in Phase 1.
        </p>
        <div className="mt-4 w-full rounded-xl border border-dashed border-border/80 bg-card/40 px-6 py-16 text-center">
          <p className="text-sm text-muted-foreground">
            Empty dashboard shell — no business data yet.
          </p>
        </div>
      </div>
    </AppShell>
  );
}
