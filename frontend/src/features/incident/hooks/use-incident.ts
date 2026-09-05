"use client";

import { useQuery } from "@tanstack/react-query";
import {
  continueIncidentInvestigation,
  getIncidentInvestigation,
} from "@/services/incident/incident-api";
import {
  INCIDENT_POLL_INTERVAL_MS,
  incidentNeedsPolling,
  incidentQueryKeys,
} from "@/features/incident/constants";
import { loadIncidentOrContinue } from "@/features/incident/lib/incident-status";

export function useIncident(id: string) {
  return useQuery({
    queryKey: incidentQueryKeys.detail(id),
    queryFn: async () => {
      const current = await getIncidentInvestigation(id);
      return loadIncidentOrContinue(current, () =>
        continueIncidentInvestigation(id),
      );
    },
    enabled: Boolean(id),
    refetchInterval: (query) =>
      incidentNeedsPolling(query.state.data?.phase)
        ? INCIDENT_POLL_INTERVAL_MS
        : false,
  });
}
