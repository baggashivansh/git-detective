import type { AnalysisStatus, RepositorySourceType } from "@/types/repository";
import type {
  InvestigationStatus,
  InvestigationTargetType,
} from "@/types/investigation";

export type IncidentPhase =
  | "ANALYZING"
  | "INVESTIGATING"
  | "VALIDATING"
  | "COMPLETED"
  | "FAILED";

export type ClaimStrength = "FACT" | "STRONG_INFERENCE" | "HYPOTHESIS";

export interface IncidentTimelineEntry {
  occurredAt: string;
  title: string;
  detail: string | null;
  commitSha: string | null;
  strength: ClaimStrength;
}

export interface IncidentEvidenceEntry {
  evidenceId: string;
  label: string;
  detail: string | null;
  strength: ClaimStrength;
}

export interface IncidentOwnershipEntry {
  contributorName: string;
  contributorEmail: string;
  ownershipPercentage: number;
  ownershipKind: string | null;
}

export interface IncidentBlastRadius {
  score: number | null;
  items: string[];
  note: string;
}

export interface IncidentClaim {
  statement: string;
  strength: ClaimStrength;
  evidenceIds: string[];
}

export interface IncidentReport {
  finding: string;
  confidence: number;
  why: string;
  timelineNote: string;
  timeline: IncidentTimelineEntry[];
  keyEvidence: IncidentEvidenceEntry[];
  affectedFiles: string[];
  affectedComponents: string[];
  codeOwnership: IncidentOwnershipEntry[];
  blastRadius: IncidentBlastRadius;
  riskFactors: string[];
  recommendedNextActions: string[];
  limitations: string[];
  claims: IncidentClaim[];
  insufficientEvidence: boolean;
}

export interface IncidentInvestigation {
  id: string;
  repositoryId: string;
  repositoryName: string;
  repositoryStatus: AnalysisStatus;
  repositoryProgressPercent: number;
  question: string;
  targetType: InvestigationTargetType;
  targetRef: string;
  targetLabel: string;
  status: InvestigationStatus;
  phase: IncidentPhase;
  summary: string | null;
  report: IncidentReport | null;
  createdAt: string;
  completedAt: string | null;
}

export interface StartIncidentInvestigationRequest {
  repositoryId?: string;
  sourceType?: RepositorySourceType;
  repository?: string;
  investigationQuestion: string;
}
