import type { IncidentPhase } from "@/types/incident";
import { isIncidentInProgress } from "@/features/incident/lib/incident-status";

export const INCIDENT_POLL_INTERVAL_MS = 1500;

export const incidentQueryKeys = {
  all: ["incident-investigations"] as const,
  list: () => [...incidentQueryKeys.all, "list"] as const,
  detail: (id: string) => [...incidentQueryKeys.all, "detail", id] as const,
};

export function incidentNeedsPolling(phase: IncidentPhase | undefined): boolean {
  return isIncidentInProgress(phase);
}
