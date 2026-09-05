import { AppShell } from "@/components/layout/app-shell";
import { IncidentStartView } from "@/features/incident/incident-start-view";

export default function InvestigatePage() {
  return (
    <AppShell title="Investigate">
      <IncidentStartView />
    </AppShell>
  );
}
