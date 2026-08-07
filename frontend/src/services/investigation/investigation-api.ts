import { apiGet, apiPost } from "@/services/investigation/api-client";
import type {
  CreateInvestigationRequest,
  InvestigationDetail,
  InvestigationReport,
  InvestigationSummary,
  ReportFormat,
} from "@/types/investigation";

export function createInvestigation(
  request: CreateInvestigationRequest,
): Promise<InvestigationSummary> {
  return apiPost<InvestigationSummary>("/investigations", request);
}

export function listInvestigations(): Promise<InvestigationSummary[]> {
  return apiGet<InvestigationSummary[]>("/investigations");
}

export function getInvestigation(id: string): Promise<InvestigationDetail> {
  return apiGet<InvestigationDetail>(`/investigations/${id}`);
}

export function getInvestigationTimeline(
  id: string,
): Promise<InvestigationDetail> {
  return apiGet<InvestigationDetail>(`/investigations/${id}/timeline`);
}

export function getInvestigationOwnership(
  id: string,
): Promise<InvestigationDetail> {
  return apiGet<InvestigationDetail>(`/investigations/${id}/ownership`);
}

export function getInvestigationImpact(
  id: string,
): Promise<InvestigationDetail> {
  return apiGet<InvestigationDetail>(`/investigations/${id}/impact`);
}

export function getInvestigationRelationships(
  id: string,
): Promise<InvestigationDetail> {
  return apiGet<InvestigationDetail>(`/investigations/${id}/relationships`);
}

export function getInvestigationReport(
  id: string,
  format: ReportFormat = "json",
): Promise<InvestigationReport> {
  const params = new URLSearchParams({ format });
  return apiGet<InvestigationReport>(
    `/investigations/${id}/report?${params}`,
  );
}
