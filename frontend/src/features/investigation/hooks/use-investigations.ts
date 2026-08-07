"use client";

import { useQuery } from "@tanstack/react-query";
import { listInvestigations } from "@/services/investigation";
import { investigationQueryKeys } from "@/features/investigation/constants";

export function useInvestigations() {
  return useQuery({
    queryKey: investigationQueryKeys.list(),
    queryFn: listInvestigations,
  });
}
