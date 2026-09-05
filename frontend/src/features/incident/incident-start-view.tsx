import { IncidentForm } from "@/components/incident/incident-form";

export function IncidentStartView() {
  return (
    <div className="mx-auto flex max-w-3xl flex-col gap-6">
      <div>
        <h2 className="text-2xl font-semibold tracking-tight">
          Incident investigator
        </h2>
        <p className="mt-1 text-muted-foreground">
          Provide a repository and a question. Git Detective analyzes the
          repository, collects evidence, and returns a structured report. It
          will not invent commits, files, or blame.
        </p>
      </div>
      <IncidentForm />
    </div>
  );
}
