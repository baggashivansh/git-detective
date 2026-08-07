package com.gitdetective.investigation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationReportResponse;
import com.gitdetective.exception.RepositoryAnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvestigationReportExporter {

    private final ObjectMapper objectMapper;

    public InvestigationReportResponse export(InvestigationDetailResponse detail, String format) {
        String normalized = format == null ? "json" : format.trim().toLowerCase();
        return switch (normalized) {
            case "json" -> new InvestigationReportResponse("json", toJson(detail));
            case "markdown", "md" ->
                    new InvestigationReportResponse("markdown", toMarkdown(detail));
            case "html" -> new InvestigationReportResponse("html", toHtml(detail));
            default ->
                    throw new RepositoryAnalysisException(
                            HttpStatus.BAD_REQUEST,
                            "UNSUPPORTED_EXPORT_FORMAT",
                            "Supported export formats: json, markdown, html");
        };
    }

    private String toJson(InvestigationDetailResponse detail) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(detail);
        } catch (JsonProcessingException exception) {
            throw new RepositoryAnalysisException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "EXPORT_FAILED",
                    "Failed to serialize investigation report",
                    exception);
        }
    }

    private String toMarkdown(InvestigationDetailResponse detail) {
        var s = detail.summary();
        StringBuilder builder = new StringBuilder();
        builder.append("# Investigation Report\n\n");
        builder.append("## Overview\n\n");
        builder.append("- ID: ").append(s.id()).append('\n');
        builder.append("- Repository: ").append(s.repositoryId()).append('\n');
        builder.append("- Target: ")
                .append(s.targetType())
                .append(" / ")
                .append(s.targetLabel())
                .append('\n');
        builder.append("- Status: ").append(s.status()).append('\n');
        builder.append("- Bus factor: ")
                .append(s.busFactorScore())
                .append(" (")
                .append(s.busFactorLevel())
                .append(")\n");
        builder.append("- Blast radius: ").append(s.blastRadiusScore()).append("\n\n");
        builder.append("## Summary\n\n").append(s.summary()).append("\n\n");

        builder.append("## Evidence\n\n");
        detail.evidence()
                .forEach(
                        item ->
                                builder.append("- [")
                                        .append(item.evidenceType())
                                        .append("] ")
                                        .append(item.label())
                                        .append(" — ")
                                        .append(item.detail())
                                        .append('\n'));

        builder.append("\n## Timeline\n\n");
        detail.timeline()
                .forEach(
                        item ->
                                builder.append("- ")
                                        .append(item.occurredAt())
                                        .append(" | ")
                                        .append(item.eventType())
                                        .append(" | ")
                                        .append(item.title())
                                        .append('\n'));

        builder.append("\n## Ownership\n\n");
        detail.ownership()
                .forEach(
                        item ->
                                builder.append("- ")
                                        .append(item.contributorName())
                                        .append(" <")
                                        .append(item.contributorEmail())
                                        .append("> — ")
                                        .append(item.ownershipPercentage())
                                        .append("% (")
                                        .append(item.ownershipKind())
                                        .append(")\n"));

        builder.append("\n## Impact\n\n");
        detail.impact()
                .forEach(
                        item ->
                                builder.append("- depth=")
                                        .append(item.dependencyDepth())
                                        .append(" ")
                                        .append(item.itemLabel())
                                        .append(" — ")
                                        .append(item.reason())
                                        .append('\n'));

        builder.append("\n## Relationships\n\n");
        detail.relationships()
                .forEach(
                        item ->
                                builder.append("- ")
                                        .append(item.sourceLabel())
                                        .append(" --")
                                        .append(item.relationshipType())
                                        .append("--> ")
                                        .append(item.targetLabel())
                                        .append('\n'));

        builder.append("\n## Statistics\n\n");
        builder.append("- Evidence count: ").append(detail.evidence().size()).append('\n');
        builder.append("- Timeline events: ").append(detail.timeline().size()).append('\n');
        builder.append("- Ownership rows: ").append(detail.ownership().size()).append('\n');
        builder.append("- Impact items: ").append(detail.impact().size()).append('\n');
        builder.append("- Relationships: ").append(detail.relationships().size()).append('\n');
        return builder.toString();
    }

    private String toHtml(InvestigationDetailResponse detail) {
        String markdown = toMarkdown(detail);
        String escaped = markdown.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="utf-8"/>
                  <title>Git Detective Investigation Report</title>
                  <style>
                    body { font-family: Georgia, serif; margin: 2rem; color: #111; }
                    pre { white-space: pre-wrap; line-height: 1.45; }
                    h1 { font-size: 1.8rem; }
                  </style>
                </head>
                <body>
                  <pre>%s</pre>
                </body>
                </html>
                """
                .formatted(escaped);
    }
}
