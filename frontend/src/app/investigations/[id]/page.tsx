import { AppShell } from "@/components/layout/app-shell";
import { InvestigationDashboardView } from "@/features/investigation/investigation-dashboard-view";

interface InvestigationDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function InvestigationDetailPage({
  params,
}: InvestigationDetailPageProps) {
  const { id } = await params;

  return (
    <AppShell title="Investigation">
      <InvestigationDashboardView investigationId={id} />
    </AppShell>
  );
}
