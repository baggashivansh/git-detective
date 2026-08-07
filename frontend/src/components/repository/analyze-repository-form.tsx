"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { FolderOpen, GitBranch } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useAnalyzeRepository } from "@/features/repository/hooks/use-analyze-repository";
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

export function AnalyzeRepositoryForm() {
  const router = useRouter();
  const analyze = useAnalyzeRepository();
  const [sourceType, setSourceType] =
    React.useState<RepositorySourceType>("GITHUB");
  const [source, setSource] = React.useState("");

  const activeOption =
    sourceOptions.find((option) => option.value === sourceType) ??
    sourceOptions[0];

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();

    const trimmed = source.trim();
    if (!trimmed) return;

    try {
      const repository = await analyze.mutateAsync({ sourceType, source: trimmed });
      router.push(`/repositories/${repository.id}`);
    } catch {
      // Error surfaced via analyze.error
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Analyze repository</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="flex flex-wrap gap-2">
            {sourceOptions.map((option) => {
              const Icon = option.icon;
              const isActive = option.value === sourceType;

              return (
                <Button
                  key={option.value}
                  type="button"
                  variant={isActive ? "default" : "outline"}
                  size="sm"
                  onClick={() => setSourceType(option.value)}
                  className={cn(!isActive && "text-muted-foreground")}
                >
                  <Icon className="size-4" />
                  {option.label}
                </Button>
              );
            })}
          </div>

          <Input
            value={source}
            onChange={(event) => setSource(event.target.value)}
            placeholder={activeOption.placeholder}
            aria-label={activeOption.label}
            disabled={analyze.isPending}
          />

          {analyze.isError ? (
            <p className="text-sm text-destructive">
              {analyze.error instanceof Error
                ? analyze.error.message
                : "Failed to start analysis"}
            </p>
          ) : null}

          <Button type="submit" disabled={analyze.isPending || !source.trim()}>
            {analyze.isPending ? "Starting analysis…" : "Analyze"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
