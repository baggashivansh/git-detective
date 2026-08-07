import {
  Progress,
  ProgressLabel,
  ProgressValue,
} from "@/components/ui/progress";
import { StatusBadge } from "@/components/repository/status-badge";
import { isAnalysisInProgress } from "@/features/repository/constants";
import type { RepositorySummary } from "@/types/repository";

interface AnalysisProgressProps {
  repository: RepositorySummary;
}

export function AnalysisProgress({ repository }: AnalysisProgressProps) {
  const inProgress = isAnalysisInProgress(repository.status);
  const showProgress = inProgress || repository.status === "FAILED";

  if (!showProgress) return null;

  return (
    <div className="rounded-xl border border-border/60 bg-card p-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <p className="text-sm font-medium">Analysis status</p>
            <StatusBadge status={repository.status} />
          </div>
          {repository.statusMessage ? (
            <p className="text-sm text-muted-foreground">
              {repository.statusMessage}
            </p>
          ) : null}
          {repository.status === "FAILED" && repository.errorMessage ? (
            <p className="text-sm text-destructive">{repository.errorMessage}</p>
          ) : null}
        </div>
        {inProgress ? (
          <p className="text-sm tabular-nums text-muted-foreground">
            {repository.progressPercent}%
          </p>
        ) : null}
      </div>

      {inProgress ? (
        <Progress value={repository.progressPercent} className="mt-4">
          <ProgressLabel className="sr-only">Analysis progress</ProgressLabel>
          <ProgressValue />
        </Progress>
      ) : null}
    </div>
  );
}
