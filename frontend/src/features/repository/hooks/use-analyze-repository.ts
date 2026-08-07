"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { analyzeRepository } from "@/services/repository";
import { repositoryQueryKeys } from "@/features/repository/constants";
import type { AnalyzeRepositoryRequest } from "@/types/repository";

export function useAnalyzeRepository() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: AnalyzeRepositoryRequest) =>
      analyzeRepository(request),
    onSuccess: () => {
      void queryClient.invalidateQueries({
        queryKey: repositoryQueryKeys.all,
      });
    },
  });
}
