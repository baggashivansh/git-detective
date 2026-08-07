"use client";

import { EmptyState } from "@/components/investigation/empty-state";
import { InvestigationCard } from "@/components/investigation/investigation-card";
import { InvestigationListSkeleton } from "@/components/investigation/investigation-skeletons";
import { useInvestigations } from "@/features/investigation/hooks/use-investigations";
import { Search } from "lucide-react";

export function InvestigationList() {
  const { data, isLoading, isError, error } = useInvestigations();

  if (isLoading) {
    return <InvestigationListSkeleton />;
  }

  if (isError) {
    return (
      <EmptyState
        title="Unable to load investigations"
        description={
          error instanceof Error
            ? error.message
            : "Something went wrong while fetching investigations."
        }
      />
    );
  }

  if (!data?.length) {
    return (
      <EmptyState
        icon={<Search className="size-8" />}
        title="No investigations yet"
        description="Create an investigation against an analyzed repository to get started."
      />
    );
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2">
      {data.map((investigation) => (
        <InvestigationCard key={investigation.id} investigation={investigation} />
      ))}
    </div>
  );
}
