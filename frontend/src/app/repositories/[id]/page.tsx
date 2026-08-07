import { AppShell } from "@/components/layout/app-shell";
import { RepositoryDashboardView } from "@/features/repository/repository-dashboard-view";

interface RepositoryDetailPageProps {
  params: Promise<{ id: string }>;
}

export default async function RepositoryDetailPage({
  params,
}: RepositoryDetailPageProps) {
  const { id } = await params;

  return (
    <AppShell title="Repository">
      <RepositoryDashboardView repositoryId={id} />
    </AppShell>
  );
}
