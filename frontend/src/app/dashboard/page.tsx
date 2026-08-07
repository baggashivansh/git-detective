import Link from "next/link";

import { AppShell } from "@/components/layout/app-shell";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export default function DashboardPage() {
  return (
    <AppShell title="Overview">
      <div className="mx-auto flex max-w-3xl flex-col items-start gap-4">
        <h2 className="text-2xl font-semibold tracking-tight">Workspace</h2>
        <p className="text-muted-foreground leading-relaxed">
          Analyze a repository, open an investigation, then ask the assistant —
          answers are validated against the Evidence Engine.
        </p>
        <div className="flex flex-wrap gap-3">
          <Link
            href="/repositories"
            className={cn(buttonVariants({ variant: "default" }))}
          >
            Repositories
          </Link>
          <Link
            href="/investigations"
            className={cn(buttonVariants({ variant: "outline" }))}
          >
            Investigations
          </Link>
          <Link
            href="/assistant"
            className={cn(buttonVariants({ variant: "outline" }))}
          >
            Assistant
          </Link>
        </div>
      </div>
    </AppShell>
  );
}
