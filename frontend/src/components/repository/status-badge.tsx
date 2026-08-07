import { Badge } from "@/components/ui/badge";
import type { AnalysisStatus } from "@/types/repository";
import { cn } from "@/lib/utils";

const statusLabels: Record<AnalysisStatus, string> = {
  QUEUED: "Queued",
  CLONING: "Cloning",
  SCANNING: "Scanning",
  PARSING: "Parsing",
  INDEXING: "Indexing",
  COMPLETED: "Completed",
  FAILED: "Failed",
};

const statusVariants: Record<
  AnalysisStatus,
  "default" | "secondary" | "destructive" | "outline"
> = {
  QUEUED: "secondary",
  CLONING: "secondary",
  SCANNING: "secondary",
  PARSING: "secondary",
  INDEXING: "secondary",
  COMPLETED: "default",
  FAILED: "destructive",
};

interface StatusBadgeProps {
  status: AnalysisStatus;
  className?: string;
}

export function StatusBadge({ status, className }: StatusBadgeProps) {
  return (
    <Badge
      variant={statusVariants[status]}
      className={cn("font-normal", className)}
    >
      {statusLabels[status]}
    </Badge>
  );
}
