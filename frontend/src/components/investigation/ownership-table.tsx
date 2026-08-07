"use client";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { BusFactorBadge } from "@/components/investigation/bus-factor-badge";
import { EmptyState } from "@/components/investigation/empty-state";
import { TableSkeleton } from "@/components/investigation/investigation-skeletons";
import {
  formatDate,
  formatNumber,
  formatPercentage,
  formatTargetType,
} from "@/features/investigation/utils/format";
import type { InvestigationSummary, OwnershipItem } from "@/types/investigation";

interface OwnershipTableProps {
  ownership: OwnershipItem[] | undefined;
  summary: InvestigationSummary | undefined;
  isLoading: boolean;
}

export function OwnershipTable({
  ownership,
  summary,
  isLoading,
}: OwnershipTableProps) {
  return (
    <Card>
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <CardTitle>Ownership</CardTitle>
          {summary ? (
            <BusFactorBadge
              level={summary.busFactorLevel}
              score={summary.busFactorScore}
            />
          ) : null}
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !ownership?.length ? (
          <EmptyState
            title="No ownership data"
            description="Ownership analysis will appear once the investigation completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Contributor</TableHead>
                <TableHead>Email</TableHead>
                <TableHead className="text-right">Total commits</TableHead>
                <TableHead className="text-right">Recent commits</TableHead>
                <TableHead className="text-right">Lines changed</TableHead>
                <TableHead className="text-right">Share</TableHead>
                <TableHead>Kind</TableHead>
                <TableHead>Last active</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {ownership.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="font-medium">
                    {item.contributorName}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {item.contributorEmail}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(item.totalCommits)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(item.recentCommits)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(item.linesChanged)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatPercentage(Number(item.ownershipPercentage))}
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {formatTargetType(item.ownershipKind)}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatDate(item.lastContributionAt)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}
