export {
  formatBytes,
  formatDate,
  formatNumber,
  formatPercentage,
  formatRelativeDate,
  shortenSha,
  truncateMiddle,
} from "@/features/repository/utils/format";

export function formatTargetType(value: string): string {
  return value
    .split("_")
    .map((part) => part.charAt(0) + part.slice(1).toLowerCase())
    .join(" ");
}

export function formatScore(value: number | null): string {
  if (value === null || value === undefined) return "—";
  return value.toFixed(1);
}
