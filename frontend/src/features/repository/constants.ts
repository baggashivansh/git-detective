import type { AnalysisStatus } from "@/types/repository";

export const IN_PROGRESS_STATUSES: AnalysisStatus[] = [
  "QUEUED",
  "CLONING",
  "SCANNING",
  "PARSING",
  "INDEXING",
];

export const POLL_INTERVAL_MS = 1500;

export function isAnalysisInProgress(status: AnalysisStatus): boolean {
  return IN_PROGRESS_STATUSES.includes(status);
}

export function isAnalysisComplete(status: AnalysisStatus): boolean {
  return status === "COMPLETED";
}

export const repositoryQueryKeys = {
  all: ["repositories"] as const,
  list: () => [...repositoryQueryKeys.all, "list"] as const,
  detail: (id: string) => [...repositoryQueryKeys.all, "detail", id] as const,
  tree: (id: string) => [...repositoryQueryKeys.all, "tree", id] as const,
  contributors: (id: string) =>
    [...repositoryQueryKeys.all, "contributors", id] as const,
  languages: (id: string) =>
    [...repositoryQueryKeys.all, "languages", id] as const,
  commits: (id: string, page: number, size: number) =>
    [...repositoryQueryKeys.all, "commits", id, page, size] as const,
  statistics: (id: string) =>
    [...repositoryQueryKeys.all, "statistics", id] as const,
  packages: (id: string) =>
    [...repositoryQueryKeys.all, "packages", id] as const,
  classes: (id: string) =>
    [...repositoryQueryKeys.all, "classes", id] as const,
  search: (id: string, query: string) =>
    [...repositoryQueryKeys.all, "search", id, query] as const,
};
