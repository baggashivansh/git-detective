import type { ReactNode } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ClaimBadge } from "@/components/incident/claim-badge";
import type { IncidentReport } from "@/types/incident";

function Section({
  title,
  children,
}: {
  title: string;
  children: ReactNode;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3 text-sm leading-relaxed">
        {children}
      </CardContent>
    </Card>
  );
}

export function IncidentReportView({ report }: { report: IncidentReport }) {
  return (
    <div className="space-y-4">
      {report.insufficientEvidence ? (
        <div className="rounded-xl border border-destructive/40 bg-destructive/5 p-4 text-sm">
          Evidence is insufficient for a confident answer. The limitations below
          explain what is missing.
        </div>
      ) : null}

      <Section title="Finding">
        <div className="flex flex-wrap items-center gap-2">
          <p className="text-muted-foreground">
            Confidence <span className="font-medium text-foreground">{report.confidence}%</span>
          </p>
        </div>
        <p className="break-words">{report.finding}</p>
      </Section>

      <Section title="Why">
        <p className="whitespace-pre-wrap break-words">{report.why}</p>
      </Section>

      <Section title="Timeline">
        <p className="text-muted-foreground">{report.timelineNote}</p>
        {report.timeline.length === 0 ? (
          <p className="text-muted-foreground">No repository timeline events.</p>
        ) : (
          <ol className="space-y-3">
            {report.timeline.map((item, index) => (
              <li key={`${item.commitSha ?? "event"}-${index}`} className="space-y-1">
                <div className="flex flex-wrap items-center gap-2">
                  <ClaimBadge strength={item.strength} />
                  <span className="font-medium">{item.title}</span>
                </div>
                <p className="text-muted-foreground">
                  {item.occurredAt}
                  {item.commitSha ? ` · ${item.commitSha}` : ""}
                </p>
                {item.detail ? <p>{item.detail}</p> : null}
              </li>
            ))}
          </ol>
        )}
      </Section>

      <Section title="Key evidence">
        {report.keyEvidence.length === 0 ? (
          <p className="text-muted-foreground">No evidence items were collected.</p>
        ) : (
          <ul className="space-y-3">
            {report.keyEvidence.map((item) => (
              <li key={item.evidenceId} className="space-y-1">
                <div className="flex flex-wrap items-center gap-2">
                  <ClaimBadge strength={item.strength} />
                  <span className="font-medium">{item.label}</span>
                </div>
                <p className="text-muted-foreground">{item.detail}</p>
              </li>
            ))}
          </ul>
        )}
      </Section>

      <Section title="Affected components">
        <div className="grid gap-4 sm:grid-cols-2">
          <div>
            <p className="mb-2 font-medium">Files</p>
            {report.affectedFiles.length === 0 ? (
              <p className="text-muted-foreground">None identified.</p>
            ) : (
              <ul className="list-disc space-y-1 pl-5">
                {report.affectedFiles.map((file) => (
                  <li key={file}>{file}</li>
                ))}
              </ul>
            )}
          </div>
          <div>
            <p className="mb-2 font-medium">Components</p>
            {report.affectedComponents.length === 0 ? (
              <p className="text-muted-foreground">None identified.</p>
            ) : (
              <ul className="list-disc space-y-1 pl-5">
                {report.affectedComponents.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </Section>

      <Section title="Code ownership">
        <p className="text-muted-foreground">
          Ownership is calculated from repository history. It is not personal
          blame.
        </p>
        {report.codeOwnership.length === 0 ? (
          <p className="text-muted-foreground">No ownership slice available.</p>
        ) : (
          <ul className="space-y-2">
            {report.codeOwnership.map((owner) => (
              <li key={owner.contributorEmail}>
                {owner.contributorName} ({owner.contributorEmail}) —{" "}
                {owner.ownershipPercentage}%
                {owner.ownershipKind ? ` · ${owner.ownershipKind}` : ""}
              </li>
            ))}
          </ul>
        )}
      </Section>

      <Section title="Blast radius">
        <p>
          Score:{" "}
          <span className="font-medium">
            {report.blastRadius.score ?? "n/a"}
          </span>
        </p>
        <p className="text-muted-foreground">{report.blastRadius.note}</p>
        {report.blastRadius.items.length > 0 ? (
          <ul className="list-disc space-y-1 pl-5">
            {report.blastRadius.items.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        ) : null}
      </Section>

      <Section title="Risk factors">
        {report.riskFactors.length === 0 ? (
          <p className="text-muted-foreground">No calculated risk factors.</p>
        ) : (
          <ul className="list-disc space-y-1 pl-5">
            {report.riskFactors.map((item) => (
              <li key={item}>{item}</li>
            ))}
          </ul>
        )}
      </Section>

      <Section title="Recommended next investigation">
        <ul className="list-disc space-y-1 pl-5">
          {report.recommendedNextActions.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </Section>

      <Section title="Limitations">
        <ul className="list-disc space-y-1 pl-5">
          {report.limitations.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </Section>

      <Section title="Claims">
        <ul className="space-y-3">
          {report.claims.map((claim, index) => (
            <li key={`${claim.strength}-${index}`} className="space-y-1">
              <ClaimBadge strength={claim.strength} />
              <p>{claim.statement}</p>
            </li>
          ))}
        </ul>
      </Section>
    </div>
  );
}
