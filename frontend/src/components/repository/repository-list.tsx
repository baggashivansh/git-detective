"use client";

import { EmptyState } from "@/components/repository/empty-state";
import { RepositoryCard } from "@/components/repository/repository-card";
import { RepositoryListSkeleton } from "@/components/repository/repository-skeletons";
import { useRepositories } from "@/features/repository/hooks/use-repositories";
import { FolderGit2 } from "lucide-react";

export function RepositoryList() {
  const { data, isLoading, isError, error } = useRepositories();

  if (isLoading) {
    return <RepositoryListSkeleton />;
  }

  if (isError) {
    return (
      <EmptyState
        title="Unable to load repositories"
        description={
          error instanceof Error
            ? error.message
            : "Something went wrong while fetching repositories."
        }
      />
    );
  }

  if (!data?.length) {
    return (
      <EmptyState
        icon={<FolderGit2 className="size-8" />}
        title="No repositories yet"
        description="Analyze a GitHub repository or local path to get started."
      />
    );
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2">
      {data.map((repository) => (
        <RepositoryCard key={repository.id} repository={repository} />
      ))}
    </div>
  );
}
