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
import { EmptyState } from "@/components/repository/empty-state";
import { TableSkeleton } from "@/components/repository/repository-skeletons";
import type { CodeType } from "@/types/repository";

interface ClassesListProps {
  classes: CodeType[] | undefined;
  isLoading: boolean;
}

export function ClassesList({ classes, isLoading }: ClassesListProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Classes & types</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !classes?.length ? (
          <EmptyState
            title="No types found"
            description="Parsed classes and interfaces will appear once analysis completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Name</TableHead>
                <TableHead>Kind</TableHead>
                <TableHead>Package</TableHead>
                <TableHead>Visibility</TableHead>
                <TableHead>Superclass</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {classes.map((codeType) => (
                <TableRow key={codeType.id}>
                  <TableCell>
                    <div className="font-medium">{codeType.name}</div>
                    <div className="font-mono text-xs text-muted-foreground">
                      {codeType.fullyQualifiedName}
                    </div>
                  </TableCell>
                  <TableCell>
                    <Badge variant="secondary">{codeType.kind}</Badge>
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {codeType.packageName ?? "—"}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {codeType.visibility ?? "—"}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {codeType.superclassName ?? "—"}
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
