"use client";

import * as React from "react";
import { Search } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/repository/empty-state";
import { useRepositorySearch } from "@/features/repository/hooks/use-repository-dashboard";
import type { AnalysisStatus, SearchHit } from "@/types/repository";

interface RepositorySearchPanelProps {
  repositoryId: string;
  status?: AnalysisStatus;
}

function SearchGroup({
  title,
  hits,
}: {
  title: string;
  hits: SearchHit[];
}) {
  if (!hits.length) return null;

  return (
    <div className="space-y-2">
      <h4 className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
        {title}
      </h4>
      <ul className="space-y-1">
        {hits.map((hit) => (
          <li
            key={`${hit.type}-${hit.id}`}
            className="rounded-md border border-border/60 px-3 py-2 text-sm"
          >
            <p className="font-medium">{hit.label}</p>
            {hit.secondary ? (
              <p className="text-xs text-muted-foreground">{hit.secondary}</p>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  );
}

export function RepositorySearchPanel({
  repositoryId,
  status,
}: RepositorySearchPanelProps) {
  const [query, setQuery] = React.useState("");
  const [debouncedQuery, setDebouncedQuery] = React.useState("");

  React.useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedQuery(query), 300);
    return () => window.clearTimeout(timer);
  }, [query]);

  const { data, isLoading, isFetching } = useRepositorySearch(
    repositoryId,
    debouncedQuery,
    status,
  );

  const hasResults =
    data &&
    (data.files.length > 0 ||
      data.folders.length > 0 ||
      data.classes.length > 0 ||
      data.packages.length > 0 ||
      data.commits.length > 0 ||
      data.branches.length > 0 ||
      data.tags.length > 0);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Search className="size-4" />
          Search repository
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <Input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search files, classes, commits…"
        />

        {debouncedQuery.trim().length < 2 ? (
          <p className="text-sm text-muted-foreground">
            Enter at least 2 characters to search.
          </p>
        ) : isLoading || isFetching ? (
          <div className="space-y-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : !hasResults ? (
          <EmptyState
            title="No results"
            description={`Nothing matched "${debouncedQuery}".`}
          />
        ) : (
          <div className="space-y-4">
            <SearchGroup title="Files" hits={data.files} />
            <SearchGroup title="Folders" hits={data.folders} />
            <SearchGroup title="Classes" hits={data.classes} />
            <SearchGroup title="Packages" hits={data.packages} />
            <SearchGroup title="Commits" hits={data.commits} />
            <SearchGroup title="Branches" hits={data.branches} />
            <SearchGroup title="Tags" hits={data.tags} />
          </div>
        )}
      </CardContent>
    </Card>
  );
}
