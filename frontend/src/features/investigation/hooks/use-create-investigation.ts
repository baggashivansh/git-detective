"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { createInvestigation } from "@/services/investigation";
import { investigationQueryKeys } from "@/features/investigation/constants";
import type { CreateInvestigationRequest } from "@/types/investigation";

export function useCreateInvestigation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateInvestigationRequest) =>
      createInvestigation(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: investigationQueryKeys.all,
      });
    },
  });
}
