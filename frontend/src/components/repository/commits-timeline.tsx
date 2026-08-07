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
import {
  formatDate,
  formatNumber,
  shortenSha,
} from "@/features/repository/utils/format";
import type { Commit } from "@/types/repository";

interface CommitsTimelineProps {
  commits: Commit[] | undefined;
  isLoading: boolean;
}

export function CommitsTimeline({ commits, isLoading }: CommitsTimelineProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Commit timeline</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton />
        ) : !commits?.length ? (
          <EmptyState
            title="No commits"
            description="Commit history will appear once analysis completes."
          />
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>SHA</TableHead>
                <TableHead>Message</TableHead>
                <TableHead>Author</TableHead>
                <TableHead>Date</TableHead>
                <TableHead className="text-right">Changes</TableHead>
                <TableHead>Refs</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {commits.map((commit) => (
                <TableRow key={commit.id}>
                  <TableCell className="font-mono text-xs">
                    {shortenSha(commit.sha)}
                  </TableCell>
                  <TableCell className="max-w-xs truncate">
                    {commit.message.split("\n")[0]}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {commit.authorName}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatDate(commit.authoredAt)}
                  </TableCell>
                  <TableCell className="text-right tabular-nums">
                    <span className="text-emerald-500">
                      +{formatNumber(commit.insertions)}
                    </span>
                    {" / "}
                    <span className="text-red-400">
                      −{formatNumber(commit.deletions)}
                    </span>
                    <span className="text-muted-foreground">
                      {" "}
                      · {formatNumber(commit.filesChangedCount)} files
                    </span>
                  </TableCell>
                  <TableCell>
                    <div className="flex flex-wrap gap-1">
                      {commit.merge ? (
                        <Badge variant="outline">merge</Badge>
                      ) : null}
                      {commit.branches.slice(0, 2).map((branch) => (
                        <Badge key={branch} variant="secondary">
                          {branch}
                        </Badge>
                      ))}
                      {commit.tags.slice(0, 2).map((tag) => (
                        <Badge key={tag} variant="outline">
                          {tag}
                        </Badge>
                      ))}
                    </div>
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
