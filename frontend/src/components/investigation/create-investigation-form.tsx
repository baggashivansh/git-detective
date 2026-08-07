"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useCreateInvestigation } from "@/features/investigation/hooks/use-create-investigation";
import { TARGET_TYPE_OPTIONS } from "@/features/investigation/constants";
import type { InvestigationTargetType } from "@/types/investigation";
import { cn } from "@/lib/utils";
import { formatTargetType } from "@/features/investigation/utils/format";

export function CreateInvestigationForm() {
  const router = useRouter();
  const create = useCreateInvestigation();
  const [repositoryId, setRepositoryId] = React.useState("");
  const [targetType, setTargetType] =
    React.useState<InvestigationTargetType>("CLASS");
  const [targetRef, setTargetRef] = React.useState("");

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();

    const trimmedRepositoryId = repositoryId.trim();
    const trimmedTargetRef = targetRef.trim();
    if (!trimmedRepositoryId || !trimmedTargetRef) return;

    try {
      const investigation = await create.mutateAsync({
        repositoryId: trimmedRepositoryId,
        targetType,
        targetRef: trimmedTargetRef,
      });
      router.push(`/investigations/${investigation.id}`);
    } catch {
      // Error surfaced via create.error
    }
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create investigation</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <label
              htmlFor="repository-id"
              className="text-sm font-medium text-foreground"
            >
              Repository ID
            </label>
            <Input
              id="repository-id"
              value={repositoryId}
              onChange={(event) => setRepositoryId(event.target.value)}
              placeholder="Repository UUID"
              disabled={create.isPending}
            />
          </div>

          <div className="space-y-2">
            <label
              htmlFor="target-type"
              className="text-sm font-medium text-foreground"
            >
              Target type
            </label>
            <select
              id="target-type"
              value={targetType}
              onChange={(event) =>
                setTargetType(event.target.value as InvestigationTargetType)
              }
              disabled={create.isPending}
              className={cn(
                "flex h-8 w-full rounded-lg border border-border bg-background px-2.5 text-sm",
                "outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
                "disabled:pointer-events-none disabled:opacity-50",
              )}
            >
              {TARGET_TYPE_OPTIONS.map((option) => (
                <option key={option} value={option}>
                  {formatTargetType(option)}
                </option>
              ))}
            </select>
          </div>

          <div className="space-y-2">
            <label
              htmlFor="target-ref"
              className="text-sm font-medium text-foreground"
            >
              Target reference
            </label>
            <Input
              id="target-ref"
              value={targetRef}
              onChange={(event) => setTargetRef(event.target.value)}
              placeholder="Fully qualified name, path, SHA, or identifier"
              disabled={create.isPending}
            />
          </div>

          {create.isError ? (
            <p className="text-sm text-destructive">
              {create.error instanceof Error
                ? create.error.message
                : "Failed to create investigation"}
            </p>
          ) : null}

          <Button
            type="submit"
            disabled={
              create.isPending || !repositoryId.trim() || !targetRef.trim()
            }
          >
            {create.isPending ? "Creating…" : "Create investigation"}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
