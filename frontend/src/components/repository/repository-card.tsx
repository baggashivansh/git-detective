import Link from "next/link";
import { GitBranch, HardDrive } from "lucide-react";
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
  truncateMiddle,
} from "@/features/repository/utils/format";
import type { RepositorySummary } from "@/types/repository";

interface RepositoryCardProps {
  repository: RepositorySummary;
}

export function RepositoryCard({ repository }: RepositoryCardProps) {
  return (
    <Link href={`/repositories/${repository.id}`} className="block">
      <Card className="transition-colors hover:bg-muted/30">
        <CardHeader>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <CardTitle className="truncate">{repository.name}</CardTitle>
              <CardDescription className="mt-1 truncate">
                {truncateMiddle(repository.sourceUri, 56)}
              </CardDescription>
            </div>
            <StatusBadge status={repository.status} />
          </div>
        </CardHeader>
        <CardContent>
          <dl className="grid grid-cols-2 gap-3 text-sm">
            <div>
              <dt className="text-muted-foreground">Source</dt>
              <dd className="mt-0.5 font-medium">{repository.sourceType}</dd>
            </div>
            <div>
              <dt className="text-muted-foreground">Language</dt>
              <dd className="mt-0.5 font-medium">
                {repository.primaryLanguage ?? "—"}
              </dd>
            </div>
            <div className="flex items-start gap-1.5">
              <GitBranch className="mt-0.5 size-3.5 text-muted-foreground" />
              <div>
                <dt className="text-muted-foreground">Commits</dt>
                <dd className="mt-0.5 font-medium">
                  {formatNumber(repository.totalCommits)}
                </dd>
              </div>
            </div>
            <div className="flex items-start gap-1.5">
              <HardDrive className="mt-0.5 size-3.5 text-muted-foreground" />
              <div>
                <dt className="text-muted-foreground">Size</dt>
                <dd className="mt-0.5 font-medium">
                  {formatBytes(repository.sizeBytes)}
                </dd>
              </div>
            </div>
          </dl>
          <p className="mt-4 text-xs text-muted-foreground">
            Updated {formatDate(repository.updatedAt)}
          </p>
        </CardContent>
      </Card>
    </Link>
  );
}
