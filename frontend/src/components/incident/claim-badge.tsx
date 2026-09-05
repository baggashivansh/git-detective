import { Badge } from "@/components/ui/badge";
import type { ClaimStrength } from "@/types/incident";

const labels: Record<ClaimStrength, string> = {
  FACT: "Fact",
  STRONG_INFERENCE: "Strong inference",
  HYPOTHESIS: "Hypothesis",
};

const variants: Record<ClaimStrength, "default" | "secondary" | "outline"> = {
  FACT: "default",
  STRONG_INFERENCE: "secondary",
  HYPOTHESIS: "outline",
};

export function ClaimBadge({ strength }: { strength: ClaimStrength }) {
  return <Badge variant={variants[strength]}>{labels[strength]}</Badge>;
}
