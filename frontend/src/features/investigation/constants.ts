import type { InvestigationStatus } from "@/types/investigation";

export const IN_PROGRESS_STATUSES: InvestigationStatus[] = ["QUEUED", "RUNNING"];

export const POLL_INTERVAL_MS = 1500;

export function isInvestigationInProgress(
  status: InvestigationStatus,
): boolean {
  return IN_PROGRESS_STATUSES.includes(status);
}

export function isInvestigationComplete(status: InvestigationStatus): boolean {
  return status === "COMPLETED";
}

export const TARGET_TYPE_OPTIONS = [
  "CLASS",
  "METHOD",
  "PACKAGE",
  "COMMIT",
  "FILE",
  "CONTRIBUTOR",
  "BRANCH",
  "TAG",
] as const;

export const investigationQueryKeys = {
  all: ["investigations"] as const,
  list: () => [...investigationQueryKeys.all, "list"] as const,
  detail: (id: string) => [...investigationQueryKeys.all, "detail", id] as const,
  timeline: (id: string) =>
    [...investigationQueryKeys.all, "timeline", id] as const,
  ownership: (id: string) =>
    [...investigationQueryKeys.all, "ownership", id] as const,
  impact: (id: string) => [...investigationQueryKeys.all, "impact", id] as const,
  relationships: (id: string) =>
    [...investigationQueryKeys.all, "relationships", id] as const,
  report: (id: string, format: string) =>
    [...investigationQueryKeys.all, "report", id, format] as const,
};
