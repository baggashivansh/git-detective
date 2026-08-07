"use client";

import { useQuery } from "@tanstack/react-query";
import {
  getRepositoryClasses,
  getRepositoryCommits,
  getRepositoryContributors,
  getRepositoryLanguages,
  getRepositoryPackages,
  getRepositoryStatistics,
  getRepositoryTree,
  searchRepository,
} from "@/services/repository";
import {
  isAnalysisComplete,
  repositoryQueryKeys,
} from "@/features/repository/constants";
import type { AnalysisStatus } from "@/types/repository";

function useEnabledForCompleteAnalysis(
  status: AnalysisStatus | undefined,
): boolean {
  return Boolean(status && isAnalysisComplete(status));
}

export function useRepositoryTree(id: string, status?: AnalysisStatus) {
  const enabled = useEnabledForCompleteAnalysis(status);

  return useQuery({
    queryKey: repositoryQueryKeys.tree(id),
    queryFn: () => getRepositoryTree(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useRepositoryContributors(
  id: string,
  status?: AnalysisStatus,
) {
  const enabled = useEnabledForCompleteAnalysis(status);

  return useQuery({
    queryKey: repositoryQueryKeys.contributors(id),
    queryFn: () => getRepositoryContributors(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useRepositoryLanguages(id: string, status?: AnalysisStatus) {
  const enabled = useEnabledForCompleteAnalysis(status);

  return useQuery({
    queryKey: repositoryQueryKeys.languages(id),
    queryFn: () => getRepositoryLanguages(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useRepositoryCommits(
  id: string,
  status?: AnalysisStatus,
  page = 0,
  size = 50,
) {
  const enabled = useEnabledForCompleteAnalysis(status);

  return useQuery({
    queryKey: repositoryQueryKeys.commits(id, page, size),
    queryFn: () => getRepositoryCommits(id, page, size),
    enabled: Boolean(id) && enabled,
  });
}

export function useRepositoryStatistics(id: string, status?: AnalysisStatus) {
  const enabled = useEnabledForCompleteAnalysis(status);

  return useQuery({
    queryKey: repositoryQueryKeys.statistics(id),
    queryFn: () => getRepositoryStatistics(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useRepositoryPackages(id: string, status?: AnalysisStatus) {
  const enabled = useEnabledForCompleteAnalysis(status);

  return useQuery({
    queryKey: repositoryQueryKeys.packages(id),
    queryFn: () => getRepositoryPackages(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useRepositoryClasses(id: string, status?: AnalysisStatus) {
  const enabled = useEnabledForCompleteAnalysis(status);

  return useQuery({
    queryKey: repositoryQueryKeys.classes(id),
    queryFn: () => getRepositoryClasses(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useRepositorySearch(
  id: string,
  query: string,
  status?: AnalysisStatus,
) {
  const enabled =
    useEnabledForCompleteAnalysis(status) && query.trim().length >= 2;

  return useQuery({
    queryKey: repositoryQueryKeys.search(id, query),
    queryFn: () => searchRepository(id, query.trim()),
    enabled: Boolean(id) && enabled,
  });
}
