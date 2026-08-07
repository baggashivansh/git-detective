"use client";

import { AnalyzeRepositoryForm } from "@/components/repository/analyze-repository-form";
import { RepositoryList } from "@/components/repository/repository-list";

export function RepositoryListView() {
  return (
    <div className="mx-auto flex max-w-5xl flex-col gap-8">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">Repositories</h2>
        <p className="mt-1 text-muted-foreground">
          Analyze GitHub repositories or local paths to build repository
          intelligence.
        </p>
      </div>

      <AnalyzeRepositoryForm />
      <RepositoryList />
    </div>
  );
}
