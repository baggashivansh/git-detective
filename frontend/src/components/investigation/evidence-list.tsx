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
import { formatTargetType } from "@/features/investigation/utils/format";
import type { EvidenceItem } from "@/types/investigation";

interface EvidenceListProps {
  evidence: EvidenceItem[] | undefined;
  isLoading: boolean;
}

export function EvidenceList({ evidence, isLoading }: EvidenceListProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Evidence</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !evidence?.length ? (
          <EmptyState
            title="No evidence"
            description="Evidence items will appear once the investigation completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Label</TableHead>
                <TableHead>Source</TableHead>
                <TableHead>Detail</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {evidence.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>
                    <Badge variant="outline">
                      {formatTargetType(item.evidenceType)}
                    </Badge>
                  </TableCell>
                  <TableCell className="font-medium">{item.label}</TableCell>
                  <TableCell className="text-muted-foreground">
                    <span className="block text-xs">{item.sourceKind}</span>
                    <span className="block font-mono text-xs">{item.sourceRef}</span>
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
