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
  formatScore,
  formatTargetType,
} from "@/features/investigation/utils/format";
import type { HotspotItem } from "@/types/investigation";

interface HotspotsTableProps {
  hotspots: HotspotItem[] | undefined;
  isLoading: boolean;
}

export function HotspotsTable({ hotspots, isLoading }: HotspotsTableProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Hotspots</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !hotspots?.length ? (
          <EmptyState
            title="No hotspots"
            description="Hotspot analysis will appear once the investigation completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="text-right">Rank</TableHead>
                <TableHead>Kind</TableHead>
                <TableHead>Label</TableHead>
                <TableHead>Reference</TableHead>
                <TableHead className="text-right">Score</TableHead>
                <TableHead>Detail</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {hotspots.map((item) => (
                <TableRow key={item.id}>
                  <TableCell className="text-right tabular-nums">
                    {item.rankPosition}
                  </TableCell>
                  <TableCell>
                    <Badge variant="outline">
                      {formatTargetType(item.hotspotKind)}
                    </Badge>
                  </TableCell>
                  <TableCell className="font-medium">{item.itemLabel}</TableCell>
                  <TableCell className="font-mono text-xs text-muted-foreground">
                    {item.itemRef}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatScore(item.score)}
                  </TableCell>
                  <TableCell className="max-w-md text-sm text-muted-foreground">
                    {item.detail ?? "—"}
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
