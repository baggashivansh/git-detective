import { AppShell } from "@/components/layout/app-shell";
import { IncidentDetailView } from "@/features/incident/incident-detail-view";

interface IncidentDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function IncidentDetailPage({
  params,
}: IncidentDetailPageProps) {
  const { id } = await params;

  return (
    <AppShell title="Incident report">
      <IncidentDetailView incidentId={id} />
    </AppShell>
  );
}
