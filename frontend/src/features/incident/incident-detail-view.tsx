"use client";

import Link from "next/link";
import { ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/investigation/empty-state";
import { IncidentProgress } from "@/components/incident/incident-progress";
import { IncidentReportView } from "@/components/incident/incident-report-view";
import { InvestigationDashboardSkeleton } from "@/components/investigation/investigation-skeletons";
import { useIncident } from "@/features/incident/hooks/use-incident";

export function IncidentDetailView({ incidentId }: { incidentId: string }) {
  const { data: incident, isLoading, isError, error } = useIncident(incidentId);

  if (isLoading) {
    return <InvestigationDashboardSkeleton />;
  }

  if (isError || !incident) {
    return (
      <EmptyState
        title="Unable to load investigation"
        description={
          error instanceof Error
            ? error.message
            : "Unable to load this investigation."
        }
      />
    );
  }

  return (
    <div className="mx-auto flex max-w-4xl flex-col gap-6">
      <Button
        variant="ghost"
        size="sm"
        className="w-fit px-0 text-muted-foreground hover:text-foreground"
        render={<Link href="/investigate" />}
      >
        <ArrowLeft className="size-4" />
        New investigation
      </Button>

      <div className="space-y-2">
        <p className="text-sm text-muted-foreground">{incident.repositoryName}</p>
        <h2 className="text-2xl font-semibold tracking-tight">
          {incident.question}
        </h2>
        <p className="text-sm text-muted-foreground">
          Target {incident.targetType} · {incident.targetLabel}
        </p>
      </div>

      <IncidentProgress incident={incident} />

      {incident.report ? <IncidentReportView report={incident.report} /> : null}
    </div>
  );
}
