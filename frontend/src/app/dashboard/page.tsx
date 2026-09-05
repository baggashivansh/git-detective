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
          Start with a repository and a question. The incident investigator
          analyzes the repo, collects evidence, and returns a cited report.
        </p>
        <div className="flex flex-wrap gap-3">
          <Link
            href="/investigate"
            className={cn(buttonVariants({ variant: "default" }))}
          >
            Investigate
          </Link>
          <Link
            href="/repositories"
            className={cn(buttonVariants({ variant: "outline" }))}
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
