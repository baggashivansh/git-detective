"use client";

import Link from "next/link";
import { CreateInvestigationForm } from "@/components/investigation/create-investigation-form";
import { InvestigationList } from "@/components/investigation/investigation-list";
import { buttonVariants } from "@/components/ui/button";
import { cn } from "@/lib/utils";

export function InvestigationListView() {
  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-8">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Investigations</h2>
        <p className="mt-1 text-muted-foreground">
          Create deterministic investigations against analyzed repositories to
          trace ownership, impact, and relationships. For a question-driven
          report, use the incident investigator.
        </p>
        <Link
          href="/investigate"
          className={cn(buttonVariants({ variant: "outline", size: "sm" }), "mt-3")}
        >
          Question-driven investigation
        </Link>
      </div>

      <CreateInvestigationForm />
      <InvestigationList />
    </div>
  );
}
