import { Progress, ProgressLabel } from "@/components/ui/progress";
import { StatusBadge } from "@/components/investigation/status-badge";
import {
  INCIDENT_STEPS,
  incidentStepIndex,
} from "@/features/incident/lib/incident-status";
import type { IncidentInvestigation } from "@/types/incident";
import { cn } from "@/lib/utils";

export function IncidentProgress({
  incident,
}: {
  incident: IncidentInvestigation;
}) {
  const step = incidentStepIndex(incident.phase);
  const percent =
    incident.phase === "ANALYZING"
      ? Math.max(8, incident.repositoryProgressPercent)
      : Math.round(((step + 1) / INCIDENT_STEPS.length) * 100);

  return (
    <div className="rounded-xl border border-border/60 bg-card p-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-1">
          <p className="text-sm font-medium">Investigation progress</p>
          <p className="text-sm text-muted-foreground">
            {incident.phase === "ANALYZING"
              ? `Analyzing ${incident.repositoryName}…`
              : incident.phase === "FAILED"
                ? incident.summary ?? "Investigation failed"
                : `Working on ${incident.repositoryName}`}
          </p>
        </div>
        <StatusBadge status={incident.status} />
      </div>

      <ol className="mt-4 flex flex-wrap gap-2">
        {INCIDENT_STEPS.map((label, index) => (
          <li
            key={label}
            className={cn(
              "rounded-full border px-2.5 py-1 text-xs",
              index <= step
                ? "border-primary/40 bg-primary/10 text-foreground"
                : "border-border text-muted-foreground",
            )}
          >
            {label}
          </li>
        ))}
      </ol>

      {incident.phase !== "COMPLETED" && incident.phase !== "FAILED" ? (
        <Progress value={percent} className="mt-4">
          <ProgressLabel className="sr-only">
            Incident investigation progress
          </ProgressLabel>
        </Progress>
      ) : null}
    </div>
  );
}
