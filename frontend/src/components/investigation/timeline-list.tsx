"use client";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/investigation/empty-state";
import { TableSkeleton } from "@/components/investigation/investigation-skeletons";
import {
  formatDate,
  formatTargetType,
  shortenSha,
} from "@/features/investigation/utils/format";
import type { TimelineItem } from "@/types/investigation";

interface TimelineListProps {
  timeline: TimelineItem[] | undefined;
  isLoading: boolean;
}

export function TimelineList({ timeline, isLoading }: TimelineListProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Timeline</CardTitle>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <TableSkeleton rows={6} />
        ) : !timeline?.length ? (
          <EmptyState
            title="No timeline events"
            description="Timeline events will appear once the investigation completes."
          />
        ) : (
          <ol className="relative space-y-0 border-l border-border/60 pl-6">
            {timeline.map((event) => (
              <li key={event.id} className="relative pb-8 last:pb-0">
                <span className="absolute -left-[25px] top-1.5 size-3 rounded-full border-2 border-primary bg-background" />
                <div className="rounded-lg border border-border/60 bg-card/40 px-4 py-3">
                  <div className="flex flex-wrap items-center gap-2">
                    <time className="text-xs text-muted-foreground">
                      {formatDate(event.occurredAt)}
                    </time>
                    <Badge variant="outline">
                      {formatTargetType(event.eventType)}
                    </Badge>
                  </div>
                  <p className="mt-1 font-medium">{event.title}</p>
                  {event.detail ? (
                    <p className="mt-1 text-sm text-muted-foreground">
                      {event.detail}
                    </p>
                  ) : null}
                  <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
                    {event.actorName ? (
                      <span>
                        {event.actorName}
                        {event.actorEmail ? ` <${event.actorEmail}>` : ""}
                      </span>
                    ) : null}
                    {event.commitSha ? (
                      <span className="font-mono">
                        {shortenSha(event.commitSha)}
                      </span>
                    ) : null}
                    {event.evidenceRef ? (
                      <span>Evidence: {event.evidenceRef}</span>
                    ) : null}
                  </div>
                </div>
              </li>
            ))}
          </ol>
        )}
      </CardContent>
    </Card>
  );
}
