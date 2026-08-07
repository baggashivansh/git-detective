import { Badge } from "@/components/ui/badge";
import type { BusFactorLevel } from "@/types/investigation";
import { cn } from "@/lib/utils";

const levelLabels: Record<BusFactorLevel, string> = {
  LOW: "Low risk",
  MEDIUM: "Medium risk",
  HIGH: "High risk",
};

const levelVariants: Record<
  BusFactorLevel,
  "default" | "secondary" | "destructive" | "outline"
> = {
  LOW: "default",
  MEDIUM: "secondary",
  HIGH: "destructive",
};

interface BusFactorBadgeProps {
  level: BusFactorLevel | null;
  score: number | null;
  className?: string;
}

export function BusFactorBadge({
  level,
  score,
  className,
}: BusFactorBadgeProps) {
  if (!level) {
    return <span className="text-muted-foreground">—</span>;
  }

  return (
    <Badge
      variant={levelVariants[level]}
      className={cn("font-normal", className)}
    >
      {levelLabels[level]}
      {score !== null ? ` (${score})` : ""}
    </Badge>
  );
}
