"use client";

import { useQuery } from "@tanstack/react-query";
import { getRepository } from "@/services/repository";
import {
  isAnalysisInProgress,
  POLL_INTERVAL_MS,
  repositoryQueryKeys,
} from "@/features/repository/constants";

export function useRepository(id: string) {
  return useQuery({
    queryKey: repositoryQueryKeys.detail(id),
    queryFn: () => getRepository(id),
    enabled: Boolean(id),
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      if (status && isAnalysisInProgress(status)) {
        return POLL_INTERVAL_MS;
      }
      return false;
    },
  });
}
