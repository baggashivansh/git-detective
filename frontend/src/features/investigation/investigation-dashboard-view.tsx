"use client";

import * as React from "react";
import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { EmptyState } from "@/components/investigation/empty-state";
import { EvidenceList } from "@/components/investigation/evidence-list";
import { HealthTable } from "@/components/investigation/health-table";
import { HotspotsTable } from "@/components/investigation/hotspots-table";
import { ImpactList } from "@/components/investigation/impact-list";
import { InvestigationDashboardSkeleton } from "@/components/investigation/investigation-skeletons";
import { InvestigationOverview } from "@/components/investigation/investigation-overview";
import { OwnershipTable } from "@/components/investigation/ownership-table";
import { RelationshipsGraph } from "@/components/investigation/relationships-graph";
import { ReportPanel } from "@/components/investigation/report-panel";
import { StatusBadge } from "@/components/investigation/status-badge";
import { TimelineList } from "@/components/investigation/timeline-list";
import { AssistantChatView } from "@/features/assistant/assistant-chat-view";
import { isInvestigationComplete } from "@/features/investigation/constants";
import {
  useInvestigationImpact,
  useInvestigationOwnership,
  useInvestigationRelationships,
  useInvestigationTimeline,
} from "@/features/investigation/hooks/use-investigation-dashboard";
import { useInvestigation } from "@/features/investigation/hooks/use-investigation";
import { formatTargetType } from "@/features/investigation/utils/format";

interface InvestigationDashboardViewProps {
  investigationId: string;
}

export function InvestigationDashboardView({
  investigationId,
}: InvestigationDashboardViewProps) {
  const { data: investigation, isLoading, isError, error } =
    useInvestigation(investigationId);

  const status = investigation?.summary.status;
  const investigationComplete = status ? isInvestigationComplete(status) : false;

  const timeline = useInvestigationTimeline(investigationId, status);
  const ownership = useInvestigationOwnership(investigationId, status);
  const impact = useInvestigationImpact(investigationId, status);
  const relationships = useInvestigationRelationships(investigationId, status);

  const [activeTab, setActiveTab] = React.useState("overview");
  const reportEnabled = activeTab === "report" && investigationComplete;

  if (isLoading) {
    return <InvestigationDashboardSkeleton />;
  }

  if (isError || !investigation) {
    return (
      <EmptyState
        title="Investigation not found"
        description={
          error instanceof Error
            ? error.message
            : "Unable to load this investigation."
        }
      />
    );
  }

  const { summary } = investigation;

  return (
    <div className="mx-auto flex max-w-6xl flex-col gap-6">
      <div className="flex flex-col gap-4">
        <Button
          variant="ghost"
          size="sm"
          className="w-fit px-0 text-muted-foreground hover:text-foreground"
          render={<Link href="/investigations" />}
        >
          <ArrowLeft className="size-4" />
          Back to investigations
        </Button>

        <div className="flex flex-wrap items-start justify-between gap-3">
          <div>
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="text-2xl font-semibold tracking-tight">
                {summary.targetLabel}
              </h2>
              <StatusBadge status={summary.status} />
            </div>
            <p className="mt-1 text-sm text-muted-foreground">
              {formatTargetType(summary.targetType)} · {summary.targetRef}
            </p>
          </div>
        </div>
      </div>

      {!investigationComplete ? (
        <EmptyState
          title="Investigation in progress"
          description="Detailed sections will unlock once the investigation completes."
        />
      ) : null}

      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        defaultValue="overview"
        className="gap-4"
      >
        <TabsList variant="line" className="w-full justify-start overflow-x-auto">
          <TabsTrigger value="overview">Overview</TabsTrigger>
          <TabsTrigger value="timeline">Timeline</TabsTrigger>
          <TabsTrigger value="evidence">Evidence</TabsTrigger>
          <TabsTrigger value="ownership">Ownership</TabsTrigger>
          <TabsTrigger value="impact">Impact</TabsTrigger>
          <TabsTrigger value="relationships">Relationships</TabsTrigger>
          <TabsTrigger value="health">Health</TabsTrigger>
          <TabsTrigger value="hotspots">Hotspots</TabsTrigger>
          <TabsTrigger value="report">Report</TabsTrigger>
          <TabsTrigger value="assistant" disabled={!investigationComplete}>
            Assistant
          </TabsTrigger>
        </TabsList>

        <TabsContent value="overview">
          <InvestigationOverview investigation={investigation} />
        </TabsContent>

        <TabsContent value="timeline">
          <TimelineList
            timeline={timeline.data?.timeline ?? investigation.timeline}
            isLoading={timeline.isLoading && investigationComplete}
          />
        </TabsContent>

        <TabsContent value="evidence">
          <EvidenceList
            evidence={investigation.evidence}
            isLoading={false}
          />
        </TabsContent>

        <TabsContent value="ownership">
          <OwnershipTable
            ownership={ownership.data?.ownership ?? investigation.ownership}
            summary={summary}
            isLoading={ownership.isLoading && investigationComplete}
          />
        </TabsContent>

        <TabsContent value="impact">
          <ImpactList
            impact={impact.data?.impact ?? investigation.impact}
            summary={summary}
            isLoading={impact.isLoading && investigationComplete}
          />
        </TabsContent>

        <TabsContent value="relationships">
          <RelationshipsGraph
            relationships={
              relationships.data?.relationships ?? investigation.relationships
            }
            isLoading={relationships.isLoading && investigationComplete}
          />
        </TabsContent>

        <TabsContent value="health">
          <HealthTable
            packageHealth={investigation.packageHealth}
            isLoading={false}
          />
        </TabsContent>

        <TabsContent value="hotspots">
          <HotspotsTable
            hotspots={investigation.hotspots}
            isLoading={false}
          />
        </TabsContent>

        <TabsContent value="report">
          <ReportPanel
            investigationId={investigationId}
            enabled={reportEnabled}
          />
        </TabsContent>

        <TabsContent value="assistant">
          {investigationComplete ? (
            <AssistantChatView investigationId={investigationId} />
          ) : (
            <EmptyState
              title="Assistant unavailable"
              description="Complete the investigation before starting an evidence-backed conversation."
            />
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}
