"use client";

import { useQuery } from "@tanstack/react-query";
import { listRepositories } from "@/services/repository";
import { repositoryQueryKeys } from "@/features/repository/constants";

export function useRepositories() {
  return useQuery({
    queryKey: repositoryQueryKeys.list(),
    queryFn: listRepositories,
  });
}
