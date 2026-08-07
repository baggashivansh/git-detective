import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { StatusBadge } from "@/components/repository/status-badge";
import {
  formatBytes,
  formatDate,
  formatNumber,
  shortenSha,
} from "@/features/repository/utils/format";
import type { RepositorySummary } from "@/types/repository";

interface RepositoryOverviewProps {
  repository: RepositorySummary;
}

export function RepositoryOverview({ repository }: RepositoryOverviewProps) {
  const items = [
    { label: "Source type", value: repository.sourceType },
    { label: "Source URI", value: repository.sourceUri },
    { label: "Remote URL", value: repository.remoteUrl ?? "—" },
    { label: "Default branch", value: repository.defaultBranch ?? "—" },
    {
      label: "Primary language",
      value: repository.primaryLanguage ?? "—",
    },
    { label: "Total commits", value: formatNumber(repository.totalCommits) },
    { label: "Size", value: formatBytes(repository.sizeBytes) },
    {
      label: "Latest commit",
      value: repository.latestCommitSha
        ? shortenSha(repository.latestCommitSha)
        : "—",
    },
    { label: "Created", value: formatDate(repository.createdAt) },
    { label: "Analyzed", value: formatDate(repository.analyzedAt) },
  ];

  return (
    <Card>
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <CardTitle>Overview</CardTitle>
            <CardDescription>Repository metadata and source details</CardDescription>
          </div>
          <StatusBadge status={repository.status} />
        </div>
      </CardHeader>
      <CardContent>
        <dl className="grid gap-4 sm:grid-cols-2">
          {items.map((item) => (
            <div key={item.label}>
              <dt className="text-xs uppercase tracking-wide text-muted-foreground">
                {item.label}
              </dt>
              <dd className="mt-1 break-all text-sm font-medium">{item.value}</dd>
            </div>
          ))}
        </dl>
      </CardContent>
    </Card>
  );
}
