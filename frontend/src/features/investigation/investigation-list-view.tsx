"use client";

import { CreateInvestigationForm } from "@/components/investigation/create-investigation-form";
import { InvestigationList } from "@/components/investigation/investigation-list";

export function InvestigationListView() {
  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-8">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Investigations</h2>
        <p className="mt-1 text-muted-foreground">
          Create deterministic investigations against analyzed repositories to
          trace ownership, impact, and relationships.
        </p>
      </div>

      <CreateInvestigationForm />
      <InvestigationList />
    </div>
  );
}
