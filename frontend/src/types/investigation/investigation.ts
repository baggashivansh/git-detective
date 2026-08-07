export type InvestigationTargetType =
  | "CLASS"
  | "METHOD"
  | "PACKAGE"
  | "COMMIT"
  | "FILE"
  | "CONTRIBUTOR"
  | "BRANCH"
  | "TAG";

export type InvestigationStatus = "QUEUED" | "RUNNING" | "COMPLETED" | "FAILED";

export type BusFactorLevel = "LOW" | "MEDIUM" | "HIGH";

export type OwnershipKind = "HISTORICAL" | "ACTIVE" | "DORMANT";

export type RiskLevel = "LOW" | "MEDIUM" | "HIGH";

export type TimelineEventType =
  | "CREATION"
  | "MODIFICATION"
  | "RENAME"
  | "MOVE"
  | "MERGE"
  | "BRANCH_MERGE"
  | "TAG_APPEARANCE"
  | "CONTRIBUTOR_CHANGE";

export type InvestigationRelationshipType =
  | "IMPORTS"
  | "IMPLEMENTS"
  | "EXTENDS"
  | "CALLS"
  | "USES"
  | "OWNS"
  | "REFERENCES"
  | "BELONGS_TO"
  | "COMMITS"
  | "MODIFIED_BY";

export type ReportFormat = "json" | "markdown" | "html";

export interface InvestigationSummary {
  id: string;
  repositoryId: string;
  targetType: InvestigationTargetType;
  targetRef: string;
  targetLabel: string;
  status: InvestigationStatus;
  summary: string | null;
  busFactorScore: number | null;
  busFactorLevel: BusFactorLevel | null;
  blastRadiusScore: number | null;
  createdAt: string;
  completedAt: string | null;
}

export interface EvidenceItem {
  id: string;
  evidenceType: string;
  sourceKind: string;
  sourceRef: string;
  label: string;
  detail: string | null;
}

export interface TimelineItem {
  id: string;
  occurredAt: string;
  eventType: TimelineEventType;
  title: string;
  detail: string | null;
  actorName: string | null;
  actorEmail: string | null;
  commitSha: string | null;
  evidenceRef: string | null;
}

export interface OwnershipItem {
  id: string;
  contributorName: string;
  contributorEmail: string;
  totalCommits: number;
  recentCommits: number;
  linesChanged: number;
  ownershipPercentage: number;
  ownershipKind: OwnershipKind;
  lastContributionAt: string | null;
}

export interface ImpactItem {
  id: string;
  itemKind: string;
  itemRef: string;
  itemLabel: string;
  dependencyDepth: number;
  reason: string | null;
}

export interface RelationshipItem {
  id: string;
  sourceKey: string;
  sourceLabel: string;
  sourceType: string;
  targetKey: string;
  targetLabel: string;
  targetType: string;
  relationshipType: InvestigationRelationshipType;
  evidenceRef: string | null;
}

export interface HotspotItem {
  id: string;
  hotspotKind: string;
  itemRef: string;
  itemLabel: string;
  score: number;
  rankPosition: number;
  detail: string | null;
}

export interface PackageHealthItem {
  id: string;
  packageName: string;
  complexityScore: number;
  dependencyCount: number;
  packageSize: number;
  modificationFrequency: number;
  contributorCount: number;
  growthScore: number;
  riskLevel: RiskLevel;
}

export interface CommitClusterItem {
  id: string;
  clusterLabel: string;
  startAt: string;
  endAt: string;
  commitCount: number;
  sharedFiles: number;
  contributors: string;
  commitShas: string;
}

export interface TraceItem {
  id: string;
  traceKind: string;
  stepOrder: number;
  stepLabel: string;
  stepRef: string;
  evidenceRef: string | null;
  detail: string | null;
}

export interface InvestigationDetail {
  summary: InvestigationSummary;
  evidence: EvidenceItem[];
  timeline: TimelineItem[];
  ownership: OwnershipItem[];
  impact: ImpactItem[];
  relationships: RelationshipItem[];
  hotspots: HotspotItem[];
  packageHealth: PackageHealthItem[];
  commitClusters: CommitClusterItem[];
  traces: TraceItem[];
}

export interface CreateInvestigationRequest {
  repositoryId: string;
  targetType: InvestigationTargetType;
  targetRef: string;
}

export interface InvestigationReport {
  format: string;
  content: string;
}
