"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { EmptyState } from "@/components/repository/empty-state";
import { formatBytes, formatNumber } from "@/features/repository/utils/format";
import type { RepositoryStatistics } from "@/types/repository";

interface StatisticsCardsProps {
  statistics: RepositoryStatistics | undefined;
  isLoading: boolean;
}

const statFields: {
  key: keyof RepositoryStatistics;
  label: string;
  format?: (value: number) => string;
}[] = [
  { key: "totalFiles", label: "Files" },
  { key: "totalDirectories", label: "Directories" },
  { key: "totalLines", label: "Lines of code" },
  { key: "totalPackages", label: "Packages" },
  { key: "totalClasses", label: "Classes" },
  { key: "totalInterfaces", label: "Interfaces" },
  { key: "totalEnums", label: "Enums" },
  { key: "totalMethods", label: "Methods" },
  { key: "totalContributors", label: "Contributors" },
  { key: "totalBranches", label: "Branches" },
  { key: "totalTags", label: "Tags" },
  { key: "totalCommits", label: "Commits" },
  { key: "binaryFileCount", label: "Binary files" },
  { key: "ignoredFileCount", label: "Ignored files" },
  { key: "sizeBytes", label: "Repository size", format: formatBytes },
];

export function StatisticsCards({
  statistics,
  isLoading,
}: StatisticsCardsProps) {
  if (isLoading) {
    return (
      <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {Array.from({ length: 8 }).map((_, index) => (
          <Skeleton key={index} className="h-24 rounded-xl" />
        ))}
      </div>
    );
  }

  if (!statistics) {
    return (
      <EmptyState
        title="No statistics yet"
        description="Repository statistics will appear once analysis completes."
      />
    );
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
      {statFields.map(({ key, label, format }) => {
        const raw = statistics[key];
        const value =
          typeof raw === "number"
            ? format
              ? format(raw)
              : formatNumber(raw)
            : "—";

        return (
          <Card key={key} size="sm">
            <CardHeader>
              <CardTitle className="text-xs font-normal text-muted-foreground">
                {label}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-2xl font-semibold tracking-tight tabular-nums">
                {value}
              </p>
            </CardContent>
          </Card>
        );
      })}
    </div>
  );
}
