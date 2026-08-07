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
import { formatNumber } from "@/features/repository/utils/format";
import type { PackageInfo } from "@/types/repository";

interface PackagesListProps {
  packages: PackageInfo[] | undefined;
  isLoading: boolean;
}

export function PackagesList({ packages, isLoading }: PackagesListProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Packages</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !packages?.length ? (
          <EmptyState
            title="No packages found"
            description="Detected packages will appear once analysis completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Path</TableHead>
                <TableHead className="text-right">Files</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {packages.map((pkg) => (
                <TableRow key={pkg.id}>
                  <TableCell className="font-medium">{pkg.name}</TableCell>
                  <TableCell className="font-mono text-xs text-muted-foreground">
                    {pkg.path}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    {formatNumber(pkg.fileCount)}
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
