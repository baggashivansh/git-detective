"use client";

import { useQuery } from "@tanstack/react-query";
import { getInvestigation } from "@/services/investigation";
import {
  isInvestigationInProgress,
  POLL_INTERVAL_MS,
  investigationQueryKeys,
} from "@/features/investigation/constants";

export function useInvestigation(id: string) {
  return useQuery({
    queryKey: investigationQueryKeys.detail(id),
    queryFn: () => getInvestigation(id),
    enabled: Boolean(id),
    refetchInterval: (query) => {
      const status = query.state.data?.summary.status;
      if (status && isInvestigationInProgress(status)) {
        return POLL_INTERVAL_MS;
      }
      return false;
    },
  });
}
