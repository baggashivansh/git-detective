"use client";

import { useQuery } from "@tanstack/react-query";
import {
  getInvestigationImpact,
  getInvestigationOwnership,
  getInvestigationRelationships,
  getInvestigationReport,
  getInvestigationTimeline,
} from "@/services/investigation";
import {
  isInvestigationComplete,
  investigationQueryKeys,
} from "@/features/investigation/constants";
import type { InvestigationStatus, ReportFormat } from "@/types/investigation";

function useEnabledForCompleteInvestigation(
  status: InvestigationStatus | undefined,
): boolean {
  return Boolean(status && isInvestigationComplete(status));
}

export function useInvestigationTimeline(
  id: string,
  status?: InvestigationStatus,
) {
  const enabled = useEnabledForCompleteInvestigation(status);

  return useQuery({
    queryKey: investigationQueryKeys.timeline(id),
    queryFn: () => getInvestigationTimeline(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useInvestigationOwnership(
  id: string,
  status?: InvestigationStatus,
) {
  const enabled = useEnabledForCompleteInvestigation(status);

  return useQuery({
    queryKey: investigationQueryKeys.ownership(id),
    queryFn: () => getInvestigationOwnership(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useInvestigationImpact(
  id: string,
  status?: InvestigationStatus,
) {
  const enabled = useEnabledForCompleteInvestigation(status);

  return useQuery({
    queryKey: investigationQueryKeys.impact(id),
    queryFn: () => getInvestigationImpact(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useInvestigationRelationships(
  id: string,
  status?: InvestigationStatus,
) {
  const enabled = useEnabledForCompleteInvestigation(status);

  return useQuery({
    queryKey: investigationQueryKeys.relationships(id),
    queryFn: () => getInvestigationRelationships(id),
    enabled: Boolean(id) && enabled,
  });
}

export function useInvestigationReport(
  id: string,
  format: ReportFormat,
  enabled: boolean,
) {
  return useQuery({
    queryKey: investigationQueryKeys.report(id, format),
    queryFn: () => getInvestigationReport(id, format),
    enabled: Boolean(id) && enabled,
  });
}
