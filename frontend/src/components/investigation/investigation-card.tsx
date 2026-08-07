import Link from "next/link";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { BusFactorBadge } from "@/components/investigation/bus-factor-badge";
import { StatusBadge } from "@/components/investigation/status-badge";
import {
  formatDate,
  formatScore,
  formatTargetType,
} from "@/features/investigation/utils/format";
import type { InvestigationSummary } from "@/types/investigation";

interface InvestigationCardProps {
  investigation: InvestigationSummary;
}

export function InvestigationCard({ investigation }: InvestigationCardProps) {
  return (
    <Link href={`/investigations/${investigation.id}`} className="block">
      <Card className="transition-colors hover:bg-muted/30">
        <CardHeader>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <CardTitle className="truncate">
                {investigation.targetLabel}
              </CardTitle>
              <CardDescription className="mt-1 truncate">
                {formatTargetType(investigation.targetType)} ·{" "}
                {investigation.targetRef}
              </CardDescription>
            </div>
            <StatusBadge status={investigation.status} />
          </div>
        </CardHeader>
        <CardContent>
          <dl className="grid grid-cols-2 gap-3 text-sm">
            <div>
              <dt className="text-muted-foreground">Repository</dt>
              <dd className="mt-0.5 truncate font-mono text-xs">
                {investigation.repositoryId}
              </dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Bus factor</dt>
              <dd className="mt-0.5">
                <BusFactorBadge
                  level={investigation.busFactorLevel}
                  score={investigation.busFactorScore}
                />
              </dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Blast radius</dt>
              <dd className="mt-0.5 font-medium tabular-nums">
                {formatScore(investigation.blastRadiusScore)}
              </dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Created</dt>
              <dd className="mt-0.5 font-medium">
                {formatDate(investigation.createdAt)}
              </dd>
            </div>
          </dl>
          {investigation.summary ? (
            <p className="mt-4 line-clamp-2 text-xs text-muted-foreground">
              {investigation.summary}
            </p>
          ) : null}
        </CardContent>
      </Card>
    </Link>
  );
}
