"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { FolderOpen, GitBranch } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useStartIncident } from "@/features/incident/hooks/use-start-incident";
import { canStartIncident } from "@/features/incident/lib/incident-status";
import { useRepositories } from "@/features/repository/hooks/use-repositories";
import type { RepositorySourceType } from "@/types/repository";
import { cn } from "@/lib/utils";

const sourceOptions: {
  value: RepositorySourceType;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  placeholder: string;
}[] = [
  {
    value: "GITHUB",
    label: "GitHub URL",
    icon: GitBranch,
    placeholder: "https://github.com/owner/repo",
  },
  {
    value: "LOCAL",
    label: "Local path",
    icon: FolderOpen,
    placeholder: "/path/to/repository",
  },
];

export function IncidentForm() {
  const router = useRouter();
  const start = useStartIncident();
  const repositories = useRepositories();
  const [sourceType, setSourceType] =
    React.useState<RepositorySourceType>("GITHUB");
  const [repository, setRepository] = React.useState("");
  const [repositoryId, setRepositoryId] = React.useState("");
  const [question, setQuestion] = React.useState("");

  const activeOption =
    sourceOptions.find((option) => option.value === sourceType) ??
    sourceOptions[0];
  const completedRepos =
    repositories.data?.filter((item) => item.status === "COMPLETED") ?? [];
  const canSubmit = canStartIncident(repositoryId || repository, question);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    if (!canSubmit) return;

    try {
      const incident = await start.mutateAsync({
        investigationQuestion: question.trim(),
        ...(repositoryId
          ? { repositoryId }
          : { sourceType, repository: repository.trim() }),
      });
      router.push(`/investigate/${incident.id}`);
    } catch {
      // Error surfaced via start.error
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Investigate an incident</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="flex flex-wrap gap-2">
            {sourceOptions.map((option) => {
              const Icon = option.icon;
              const isActive = option.value === sourceType && !repositoryId;
              return (
                <Button
                  key={option.value}
                  type="button"
                  variant={isActive ? "default" : "outline"}
                  size="sm"
                  onClick={() => {
                    setSourceType(option.value);
                    setRepositoryId("");
                  }}
                  className={cn(!isActive && "text-muted-foreground")}
                >
                  <Icon className="size-4" />
                  {option.label}
                </Button>
              );
            })}
          </div>

          {completedRepos.length > 0 ? (
            <div className="space-y-2">
              <label
                htmlFor="existing-repository"
                className="text-sm font-medium"
              >
                Or reuse an analyzed repository
              </label>
              <select
                id="existing-repository"
                value={repositoryId}
                onChange={(event) => setRepositoryId(event.target.value)}
                disabled={start.isPending}
                className={cn(
                  "flex h-8 w-full rounded-lg border border-border bg-background px-2.5 text-sm",
                  "outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
                )}
              >
                <option value="">Use a new source below</option>
                {completedRepos.map((item) => (
                  <option key={item.id} value={item.id}>
                    {item.name}
                  </option>
                ))}
              </select>
            </div>
          ) : null}

          {!repositoryId ? (
            <Input
              value={repository}
              onChange={(event) => setRepository(event.target.value)}
              placeholder={activeOption.placeholder}
              aria-label={activeOption.label}
              disabled={start.isPending}
            />
          ) : null}

          <div className="space-y-2">
            <label htmlFor="investigation-question" className="text-sm font-medium">
              Investigation question
            </label>
            <textarea
              id="investigation-question"
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              placeholder="Why did authentication become risky after the recent changes?"
              disabled={start.isPending}
              maxLength={2000}
              rows={4}
              className={cn(
                "flex min-h-24 w-full rounded-lg border border-border bg-background px-2.5 py-2 text-sm",
                "outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
                "disabled:pointer-events-none disabled:opacity-50",
              )}
            />
          </div>

          {start.isError ? (
            <p className="text-sm text-destructive">
              {start.error instanceof Error
                ? start.error.message
                : "Failed to start investigation"}
            </p>
          ) : null}

          <Button type="submit" disabled={start.isPending || !canSubmit}>
            {start.isPending ? "Starting…" : "Investigate"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
