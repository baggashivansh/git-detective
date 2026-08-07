package com.gitdetective.evidence.mapper;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.evidence.model.EvidenceCategory;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Maps investigation-layer strings/enums into evidence-layer abstractions.
 *
 * <p>Never returns investigation JPA entities.
 */
@Component
public class InvestigationEvidenceMapper {

    public EvidenceCategory mapEvidenceType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return EvidenceCategory.STATISTIC;
        }
        try {
            return EvidenceCategory.valueOf(rawType.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return switch (rawType.trim().toUpperCase(Locale.ROOT)) {
                case "TIMELINE" -> EvidenceCategory.TIMELINE;
                case "OWNERSHIP" -> EvidenceCategory.OWNERSHIP;
                case "IMPACT" -> EvidenceCategory.IMPACT;
                case "FILE_HISTORY" -> EvidenceCategory.FILE;
                default -> EvidenceCategory.STATISTIC;
            };
        }
    }

    public EvidenceCategory mapTargetType(String targetType) {
        if (targetType == null) {
            return EvidenceCategory.TARGET;
        }
        return switch (targetType.toUpperCase(Locale.ROOT)) {
            case "CLASS" -> EvidenceCategory.CLASS;
            case "METHOD" -> EvidenceCategory.METHOD;
            case "PACKAGE" -> EvidenceCategory.PACKAGE;
            case "COMMIT" -> EvidenceCategory.COMMIT;
            case "FILE" -> EvidenceCategory.FILE;
            case "CONTRIBUTOR" -> EvidenceCategory.CONTRIBUTOR;
            default -> EvidenceCategory.TARGET;
        };
    }

    public String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public String requireRef(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return "unknown";
    }

    public InvestigationDetailResponse requireDetail(InvestigationDetailResponse detail) {
        if (detail == null || detail.summary() == null) {
            throw new IllegalArgumentException("Investigation detail is required");
        }
        return detail;
    }
}
