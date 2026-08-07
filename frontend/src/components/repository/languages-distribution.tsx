"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/repository/empty-state";
import { TableSkeleton } from "@/components/repository/repository-skeletons";
import {
  formatBytes,
  formatNumber,
  formatPercentage,
} from "@/features/repository/utils/format";
import type { LanguageStatistic } from "@/types/repository";

interface LanguagesDistributionProps {
  languages: LanguageStatistic[] | undefined;
  isLoading: boolean;
}

export function LanguagesDistribution({
  languages,
  isLoading,
}: LanguagesDistributionProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Languages</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton rows={4} />
        ) : !languages?.length ? (
          <EmptyState
            title="No language data"
            description="Language distribution will appear once analysis completes."
          />
        ) : (
          <div className="space-y-4">
            {languages.map((language) => (
              <div key={language.language} className="space-y-2">
                <div className="flex items-center justify-between gap-3 text-sm">
                  <span className="font-medium">{language.language}</span>
                  <span className="text-muted-foreground tabular-nums">
                    {formatPercentage(Number(language.percentage))} ·{" "}
                    {formatNumber(language.fileCount)} files ·{" "}
                    {formatNumber(language.lineCount)} lines
                  </span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-muted">
                  <div
                    className="h-full rounded-full bg-primary transition-all"
                    style={{
                      width: `${Math.min(Number(language.percentage), 100)}%`,
                    }}
                  />
                </div>
                <p className="text-xs text-muted-foreground">
                  {formatBytes(language.byteCount)}
                </p>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
