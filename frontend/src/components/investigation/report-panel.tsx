"use client";

import * as React from "react";
import { Copy, Download } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { EmptyState } from "@/components/investigation/empty-state";
import { TableSkeleton } from "@/components/investigation/investigation-skeletons";
import { useInvestigationReport } from "@/features/investigation/hooks/use-investigation-dashboard";
import type { ReportFormat } from "@/types/investigation";
import { cn } from "@/lib/utils";

const FORMAT_OPTIONS: { value: ReportFormat; label: string }[] = [
  { value: "json", label: "JSON" },
  { value: "markdown", label: "Markdown" },
  { value: "html", label: "HTML" },
];

interface ReportPanelProps {
  investigationId: string;
  enabled: boolean;
}

export function ReportPanel({ investigationId, enabled }: ReportPanelProps) {
  const [format, setFormat] = React.useState<ReportFormat>("json");
  const [copied, setCopied] = React.useState(false);

  const report = useInvestigationReport(investigationId, format, enabled);

  async function handleCopy() {
    if (!report.data?.content) return;

    try {
      await navigator.clipboard.writeText(report.data.content);
      setCopied(true);
      window.setTimeout(() => setCopied(false), 2000);
    } catch {
      setCopied(false);
    }
  }

  function handleDownload() {
    if (!report.data?.content) return;

    const mimeTypes: Record<ReportFormat, string> = {
      json: "application/json",
      markdown: "text/markdown",
      html: "text/html",
    };

    const extensions: Record<ReportFormat, string> = {
      json: "json",
      markdown: "md",
      html: "html",
    };

    const blob = new Blob([report.data.content], {
      type: mimeTypes[format],
    });
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = `investigation-${investigationId}.${extensions[format]}`;
    anchor.click();
    URL.revokeObjectURL(url);
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex flex-wrap items-center justify-between gap-3">
          <CardTitle>Report</CardTitle>
          <div className="flex flex-wrap items-center gap-2">
            <select
              value={format}
              onChange={(event) =>
                setFormat(event.target.value as ReportFormat)
              }
              className={cn(
                "h-8 rounded-lg border border-border bg-background px-2.5 text-sm",
                "outline-none focus-visible:border-ring focus-visible:ring-3 focus-visible:ring-ring/50",
              )}
            >
              {FORMAT_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleCopy}
              disabled={!report.data?.content || report.isLoading}
            >
              <Copy className="size-4" />
              {copied ? "Copied" : "Copy"}
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={handleDownload}
              disabled={!report.data?.content || report.isLoading}
            >
              <Download className="size-4" />
              Download
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {!enabled ? (
          <EmptyState
            title="Investigation in progress"
            description="Reports are available once the investigation completes."
          />
        ) : report.isLoading ? (
          <TableSkeleton rows={8} />
        ) : report.isError ? (
          <EmptyState
            title="Unable to load report"
            description={
              report.error instanceof Error
                ? report.error.message
                : "Something went wrong while fetching the report."
            }
          />
        ) : !report.data?.content ? (
          <EmptyState
            title="No report content"
            description="The report endpoint returned no content for this format."
          />
        ) : format === "html" ? (
          <div className="overflow-auto rounded-lg border border-border/60 bg-background p-4">
            <iframe
              title="Investigation report"
              srcDoc={report.data.content}
              className="h-[480px] w-full rounded-md border-0 bg-white"
              sandbox=""
            />
          </div>
        ) : (
          <pre className="max-h-[480px] overflow-auto rounded-lg border border-border/60 bg-muted/20 p-4 text-xs leading-relaxed">
            <code>{report.data.content}</code>
          </pre>
        )}
      </CardContent>
    </Card>
  );
}
