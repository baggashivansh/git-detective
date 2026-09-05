"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { startIncidentInvestigation } from "@/services/incident/incident-api";
import { incidentQueryKeys } from "@/features/incident/constants";
import type { StartIncidentInvestigationRequest } from "@/types/incident";

export function useStartIncident() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: StartIncidentInvestigationRequest) =>
      startIncidentInvestigation(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: incidentQueryKeys.all });
    },
  });
}
