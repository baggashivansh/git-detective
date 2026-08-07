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
import { EmptyState } from "@/components/investigation/empty-state";
import { TableSkeleton } from "@/components/investigation/investigation-skeletons";
import {
  formatNumber,
  formatScore,
  formatTargetType,
} from "@/features/investigation/utils/format";
import type { PackageHealthItem } from "@/types/investigation";
import { cn } from "@/lib/utils";

interface HealthTableProps {
  packageHealth: PackageHealthItem[] | undefined;
  isLoading: boolean;
}

const riskVariants: Record<
  PackageHealthItem["riskLevel"],
  "default" | "secondary" | "destructive"
> = {
  LOW: "default",
  MEDIUM: "secondary",
  HIGH: "destructive",
};

export function HealthTable({ packageHealth, isLoading }: HealthTableProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Package health</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !packageHealth?.length ? (
          <EmptyState
            title="No package health data"
            description="Package health metrics will appear once the investigation completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Package</TableHead>
                <TableHead className="text-right">Complexity</TableHead>
                <TableHead className="text-right">Dependencies</TableHead>
                <TableHead className="text-right">Size</TableHead>
                <TableHead className="text-right">Mod frequency</TableHead>
                <TableHead className="text-right">Contributors</TableHead>
                <TableHead className="text-right">Growth</TableHead>
                <TableHead>Risk</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {packageHealth.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="font-medium">{item.packageName}</TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatScore(item.complexityScore)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(item.dependencyCount)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(item.packageSize)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatScore(item.modificationFrequency)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(item.contributorCount)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatScore(item.growthScore)}
                  </TableCell>
                  <TableCell>
                    <Badge
                      variant={riskVariants[item.riskLevel]}
                      className={cn("font-normal")}
                    >
                      {formatTargetType(item.riskLevel)}
                    </Badge>
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
