"use client";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { BusFactorBadge } from "@/components/investigation/bus-factor-badge";
import { StatusBadge } from "@/components/investigation/status-badge";
import {
  formatDate,
  formatScore,
  formatTargetType,
} from "@/features/investigation/utils/format";
import type { InvestigationDetail } from "@/types/investigation";

interface InvestigationOverviewProps {
  investigation: InvestigationDetail;
}

export function InvestigationOverview({
  investigation,
}: InvestigationOverviewProps) {
  const { summary } = investigation;

  return (
    <div className="space-y-4">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Status
            </CardTitle>
          </CardHeader>
          <CardContent>
            <StatusBadge status={summary.status} />
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Bus factor
            </CardTitle>
          </CardHeader>
          <CardContent>
            <BusFactorBadge
              level={summary.busFactorLevel}
              score={summary.busFactorScore}
            />
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Blast radius
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold tabular-nums">
              {formatScore(summary.blastRadiusScore)}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium text-muted-foreground">
              Evidence items
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-2xl font-semibold tabular-nums">
              {investigation.evidence.length}
            </p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Target</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <dl className="grid gap-3 sm:grid-cols-2">
            <div>
              <dt className="text-sm text-muted-foreground">Label</dt>
              <dd className="mt-0.5 font-medium">{summary.targetLabel}</dd>
            </div>
            <div>
              <dt className="text-sm text-muted-foreground">Type</dt>
              <dd className="mt-0.5">
                <Badge variant="outline">
                  {formatTargetType(summary.targetType)}
                </Badge>
              </dd>
            </div>
            <div>
              <dt className="text-sm text-muted-foreground">Reference</dt>
              <dd className="mt-0.5 break-all font-mono text-sm">
                {summary.targetRef}
              </dd>
            </div>
            <div>
              <dt className="text-sm text-muted-foreground">Repository</dt>
              <dd className="mt-0.5 break-all font-mono text-xs">
                {summary.repositoryId}
              </dd>
            </div>
            <div>
              <dt className="text-sm text-muted-foreground">Created</dt>
              <dd className="mt-0.5">{formatDate(summary.createdAt)}</dd>
            </div>
            <div>
              <dt className="text-sm text-muted-foreground">Completed</dt>
              <dd className="mt-0.5">{formatDate(summary.completedAt)}</dd>
            </div>
          </dl>

          {summary.summary ? (
            <div>
              <dt className="text-sm text-muted-foreground">Summary</dt>
              <dd className="mt-1 text-sm">{summary.summary}</dd>
            </div>
          ) : null}
        </CardContent>
      </Card>

      {investigation.traces.length > 0 ? (
        <Card>
          <CardHeader>
            <CardTitle>Traces</CardTitle>
          </CardHeader>
          <CardContent>
            <ol className="space-y-3">
              {investigation.traces.map((trace) => (
                <li
                  key={trace.id}
                  className="rounded-lg border border-border/60 bg-muted/20 px-4 py-3"
                >
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-xs font-medium text-muted-foreground">
                      Step {trace.stepOrder}
                    </span>
                    <Badge variant="outline">{trace.traceKind}</Badge>
                  </div>
                  <p className="mt-1 font-medium">{trace.stepLabel}</p>
                  {trace.stepRef ? (
                    <p className="mt-0.5 font-mono text-xs text-muted-foreground">
                      {trace.stepRef}
                    </p>
                  ) : null}
                  {trace.detail ? (
                    <p className="mt-2 text-sm text-muted-foreground">
                      {trace.detail}
                    </p>
                  ) : null}
                </li>
              ))}
            </ol>
          </CardContent>
        </Card>
      ) : null}

      {investigation.commitClusters.length > 0 ? (
        <Card>
          <CardHeader>
            <CardTitle>Commit clusters</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {investigation.commitClusters.map((cluster) => (
                <div
                  key={cluster.id}
                  className="rounded-lg border border-border/60 px-4 py-3"
                >
                  <p className="font-medium">{cluster.clusterLabel}</p>
                  <dl className="mt-2 grid gap-2 text-sm sm:grid-cols-2">
                    <div>
                      <dt className="text-muted-foreground">Period</dt>
                      <dd>
                        {formatDate(cluster.startAt)} –{" "}
                        {formatDate(cluster.endAt)}
                      </dd>
                    </div>
                    <div>
                      <dt className="text-muted-foreground">Commits</dt>
                      <dd>{cluster.commitCount}</dd>
                    </div>
                    <div>
                      <dt className="text-muted-foreground">Shared files</dt>
                      <dd>{cluster.sharedFiles}</dd>
                    </div>
                    <div>
                      <dt className="text-muted-foreground">Contributors</dt>
                      <dd>{cluster.contributors}</dd>
                    </div>
                  </dl>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      ) : null}
    </div>
  );
}
