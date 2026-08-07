"use client";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/investigation/empty-state";
import { TableSkeleton } from "@/components/investigation/investigation-skeletons";
import {
  formatScore,
  formatTargetType,
} from "@/features/investigation/utils/format";
import type { ImpactItem, InvestigationSummary } from "@/types/investigation";

interface ImpactListProps {
  impact: ImpactItem[] | undefined;
  summary: InvestigationSummary | undefined;
  isLoading: boolean;
}

export function ImpactList({ impact, summary, isLoading }: ImpactListProps) {
  return (
    <Card>
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <CardTitle>Impact</CardTitle>
          {summary?.blastRadiusScore !== null &&
          summary?.blastRadiusScore !== undefined ? (
            <Badge variant="secondary">
              Blast radius: {formatScore(summary.blastRadiusScore)}
            </Badge>
          ) : null}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton rows={6} />
        ) : !impact?.length ? (
          <EmptyState
            title="No impact items"
            description="Impact analysis will appear once the investigation completes."
          />
        ) : (
          <div className="space-y-3">
            {impact.map((item) => (
              <div
                key={item.id}
                className="rounded-lg border border-border/60 px-4 py-3"
              >
                <div className="flex flex-wrap items-center gap-2">
                  <Badge variant="outline">
                    {formatTargetType(item.itemKind)}
                  </Badge>
                  <Badge variant="secondary">Depth {item.dependencyDepth}</Badge>
                </div>
                <p className="mt-2 font-medium">{item.itemLabel}</p>
                <p className="mt-0.5 font-mono text-xs text-muted-foreground">
                  {item.itemRef}
                </p>
                {item.reason ? (
                  <p className="mt-2 text-sm text-muted-foreground">
                    {item.reason}
                  </p>
                ) : null}
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
