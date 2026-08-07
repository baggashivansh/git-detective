import { Badge } from "@/components/ui/badge";
import type { InvestigationStatus } from "@/types/investigation";
import { cn } from "@/lib/utils";

const statusLabels: Record<InvestigationStatus, string> = {
  QUEUED: "Queued",
  RUNNING: "Running",
  COMPLETED: "Completed",
  FAILED: "Failed",
};

const statusVariants: Record<
  InvestigationStatus,
  "default" | "secondary" | "destructive" | "outline"
> = {
  QUEUED: "secondary",
  RUNNING: "secondary",
  COMPLETED: "default",
  FAILED: "destructive",
};

interface StatusBadgeProps {
  status: InvestigationStatus;
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
