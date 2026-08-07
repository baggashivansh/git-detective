import { AppShell } from "@/components/layout/app-shell";
import { RepositoryListView } from "@/features/repository/repository-list-view";

export default function RepositoriesPage() {
  return (
    <AppShell title="Repositories">
      <RepositoryListView />
    </AppShell>
  );
}
