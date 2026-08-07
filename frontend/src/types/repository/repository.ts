export type RepositorySourceType = "GITHUB" | "LOCAL";

export type AnalysisStatus =
  | "QUEUED"
  | "CLONING"
  | "SCANNING"
  | "PARSING"
  | "INDEXING"
  | "COMPLETED"
  | "FAILED";

export type CodeTypeKind =
  | "CLASS"
  | "INTERFACE"
  | "ENUM"
  | "ANNOTATION"
  | "RECORD";

export interface RepositorySummary {
  id: string;
  name: string;
  sourceType: RepositorySourceType;
  sourceUri: string;
  remoteUrl: string | null;
  defaultBranch: string | null;
  totalCommits: number;
  sizeBytes: number;
  primaryLanguage: string | null;
  status: AnalysisStatus;
  statusMessage: string | null;
  progressPercent: number;
  errorCode: string | null;
  errorMessage: string | null;
  latestCommitSha: string | null;
  createdAt: string;
  updatedAt: string;
  analyzedAt: string | null;
}

export interface RepositoryTreeNode {
  id: string;
  path: string;
  name: string;
  parentPath: string | null;
  directory: boolean;
  language: string | null;
  extension: string | null;
  sizeBytes: number;
  children: RepositoryTreeNode[];
}

export interface Contributor {
  id: string;
  name: string;
  email: string;
  commitCount: number;
  filesModified: number;
  linesAdded: number;
  linesDeleted: number;
  lastContributionAt: string | null;
  contributionPercentage: number;
}

export interface LanguageStatistic {
  language: string;
  fileCount: number;
  lineCount: number;
  byteCount: number;
  percentage: number;
}

export interface Commit {
  id: string;
  sha: string;
  authorName: string;
  authorEmail: string;
  authoredAt: string;
  message: string;
  merge: boolean;
  insertions: number;
  deletions: number;
  filesChangedCount: number;
  parents: string[];
  branches: string[];
  tags: string[];
}

export interface RepositoryStatistics {
  repositoryId: string;
  totalFiles: number;
  totalDirectories: number;
  totalLines: number;
  totalPackages: number;
  totalClasses: number;
  totalInterfaces: number;
  totalEnums: number;
  totalMethods: number;
  totalContributors: number;
  totalBranches: number;
  totalTags: number;
  binaryFileCount: number;
  ignoredFileCount: number;
  totalCommits: number;
  sizeBytes: number;
}

export interface PackageInfo {
  id: string;
  name: string;
  path: string;
  fileCount: number;
}

export interface CodeType {
  id: string;
  name: string;
  fullyQualifiedName: string;
  kind: CodeTypeKind;
  visibility: string | null;
  superclassName: string | null;
  packageName: string | null;
}

export interface SearchHit {
  type: string;
  id: string;
  label: string;
  secondary: string | null;
}

export interface SearchResult {
  query: string;
  files: SearchHit[];
  folders: SearchHit[];
  classes: SearchHit[];
  packages: SearchHit[];
  commits: SearchHit[];
  branches: SearchHit[];
  tags: SearchHit[];
}

export interface AnalyzeRepositoryRequest {
  sourceType: RepositorySourceType;
  source: string;
}
