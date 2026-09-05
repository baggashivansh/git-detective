import { apiGet, apiPost } from "@/services/investigation/api-client";
import type {
  IncidentInvestigation,
  StartIncidentInvestigationRequest,
} from "@/types/incident";

export function startIncidentInvestigation(
  request: StartIncidentInvestigationRequest,
): Promise<IncidentInvestigation> {
  return apiPost<IncidentInvestigation>("/incident-investigations", request);
}

export function listIncidentInvestigations(): Promise<IncidentInvestigation[]> {
  return apiGet<IncidentInvestigation[]>("/incident-investigations");
}

export function getIncidentInvestigation(
  id: string,
): Promise<IncidentInvestigation> {
  return apiGet<IncidentInvestigation>(`/incident-investigations/${id}`);
}

export function continueIncidentInvestigation(
  id: string,
): Promise<IncidentInvestigation> {
  return apiPost<IncidentInvestigation>(
    `/incident-investigations/${id}/continue`,
    {},
  );
}
