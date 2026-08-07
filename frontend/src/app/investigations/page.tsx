import { AppShell } from "@/components/layout/app-shell";
import { InvestigationListView } from "@/features/investigation/investigation-list-view";

export default function InvestigationsPage() {
  return (
    <AppShell title="Investigations">
      <InvestigationListView />
    </AppShell>
  );
}
