"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { EmptyState } from "@/components/repository/empty-state";
import { TableSkeleton } from "@/components/repository/repository-skeletons";
import {
  formatDate,
  formatNumber,
  formatPercentage,
} from "@/features/repository/utils/format";
import type { Contributor } from "@/types/repository";

interface ContributorsTableProps {
  contributors: Contributor[] | undefined;
  isLoading: boolean;
}

export function ContributorsTable({
  contributors,
  isLoading,
}: ContributorsTableProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Contributors</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !contributors?.length ? (
          <EmptyState
            title="No contributors"
            description="Contributor statistics will appear once analysis completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead className="text-right">Commits</TableHead>
                <TableHead className="text-right">Files</TableHead>
                <TableHead className="text-right">+Lines</TableHead>
                <TableHead className="text-right">−Lines</TableHead>
                <TableHead className="text-right">Share</TableHead>
                <TableHead>Last active</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {contributors.map((contributor) => (
                <TableRow key={contributor.id}>
                  <TableCell className="font-medium">
                    {contributor.name}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {contributor.email}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(contributor.commitCount)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(contributor.filesModified)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums text-emerald-500">
                    +{formatNumber(contributor.linesAdded)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums text-red-400">
                    −{formatNumber(contributor.linesDeleted)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatPercentage(Number(contributor.contributionPercentage))}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatDate(contributor.lastContributionAt)}
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
