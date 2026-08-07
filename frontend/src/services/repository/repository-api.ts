import { apiGet, apiPost } from "@/services/repository/api-client";
import type {
  AnalyzeRepositoryRequest,
  CodeType,
  Commit,
  Contributor,
  LanguageStatistic,
  PackageInfo,
  RepositoryStatistics,
  RepositorySummary,
  RepositoryTreeNode,
  SearchResult,
} from "@/types/repository";

export function analyzeRepository(
  request: AnalyzeRepositoryRequest,
): Promise<RepositorySummary> {
  return apiPost<RepositorySummary>("/repositories/analyze", request);
}

export function listRepositories(): Promise<RepositorySummary[]> {
  return apiGet<RepositorySummary[]>("/repositories");
}

export function getRepository(id: string): Promise<RepositorySummary> {
  return apiGet<RepositorySummary>(`/repositories/${id}`);
}

export function getRepositoryTree(id: string): Promise<RepositoryTreeNode[]> {
  return apiGet<RepositoryTreeNode[]>(`/repositories/${id}/tree`);
}

export function getRepositoryContributors(
  id: string,
): Promise<Contributor[]> {
  return apiGet<Contributor[]>(`/repositories/${id}/contributors`);
}

export function getRepositoryLanguages(
  id: string,
): Promise<LanguageStatistic[]> {
  return apiGet<LanguageStatistic[]>(`/repositories/${id}/languages`);
}

export function getRepositoryCommits(
  id: string,
  page = 0,
  size = 50,
): Promise<Commit[]> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });
  return apiGet<Commit[]>(`/repositories/${id}/commits?${params}`);
}

export function getRepositoryStatistics(
  id: string,
): Promise<RepositoryStatistics> {
  return apiGet<RepositoryStatistics>(`/repositories/${id}/statistics`);
}

export function getRepositoryPackages(id: string): Promise<PackageInfo[]> {
  return apiGet<PackageInfo[]>(`/repositories/${id}/packages`);
}

export function getRepositoryClasses(id: string): Promise<CodeType[]> {
  return apiGet<CodeType[]>(`/repositories/${id}/classes`);
}

export function searchRepository(
  id: string,
  query: string,
): Promise<SearchResult> {
  const params = new URLSearchParams({ q: query });
  return apiGet<SearchResult>(`/repositories/${id}/search?${params}`);
}
