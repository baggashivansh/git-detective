export const INCIDENT_STEPS = [
  "Analyze",
  "Collect evidence",
  "Investigate",
  "Validate",
  "Report",
] as const;

export type IncidentPhase =
  | "ANALYZING"
  | "INVESTIGATING"
  | "VALIDATING"
  | "COMPLETED"
  | "FAILED";

export function incidentStepIndex(phase: IncidentPhase | undefined): number {
  switch (phase) {
    case "ANALYZING":
      return 0;
    case "INVESTIGATING":
      return 2;
    case "VALIDATING":
      return 3;
    case "COMPLETED":
      return 4;
    case "FAILED":
      return 0;
    default:
      return 0;
  }
}

export function shouldContinueIncident(input: {
  phase: IncidentPhase;
  status: string;
  report: unknown;
}): boolean {
  if (
    input.phase === "FAILED" ||
    input.phase === "COMPLETED" ||
    input.phase === "INVESTIGATING"
  ) {
    return false;
  }
  if (input.phase === "ANALYZING" || input.status === "QUEUED") {
    return true;
  }
  return input.status === "COMPLETED" && input.report == null;
}

export async function loadIncidentOrContinue<T extends {
  phase: IncidentPhase;
  status: string;
  report: unknown;
}>(current: T, continueFn: () => Promise<T>): Promise<T> {
  if (!shouldContinueIncident(current)) {
    return current;
  }
  try {
    return await continueFn();
  } catch {
    return current;
  }
}

export function isIncidentInProgress(phase: IncidentPhase | undefined): boolean {
  return (
    phase === "ANALYZING" ||
    phase === "INVESTIGATING" ||
    phase === "VALIDATING"
  );
}

export function canStartIncident(repository: string, question: string): boolean {
  return repository.trim().length > 0 && question.trim().length > 0;
}
