package com.gitdetective.evidence.validator;

import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.model.EvidenceConfidenceRules;
import com.gitdetective.evidence.model.EvidenceProvenance;
import com.gitdetective.evidence.model.EvidenceRecord;
import com.gitdetective.evidence.model.EvidenceVerificationStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Validates evidence records and assembled bundles. Invalid bundles are never published. */
@Component
public class EvidenceValidator {

    public List<EvidenceRecord> validateAndMark(
            List<EvidenceRecord> records, UUID repositoryId, UUID investigationId) {
        List<EvidenceRecord> validated = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        Instant now = Instant.now();

        for (EvidenceRecord record : records) {
            List<String> errors = validateRecord(record, repositoryId, investigationId, now);
            if (!errors.isEmpty()) {
                throw new EvidenceValidationException(
                        "Invalid evidence item "
                                + record.evidenceId()
                                + ": "
                                + String.join("; ", errors));
            }
            if (!seen.add(record.dedupeKey())) {
                continue;
            }
            validated.add(record.withVerificationStatus(EvidenceVerificationStatus.VERIFIED));
        }
        return validated;
    }

    public void validateBundle(EvidenceBundle bundle) {
        if (bundle == null) {
            throw new EvidenceValidationException("Evidence bundle is null");
        }
        if (bundle.allEvidence().isEmpty()) {
            throw new EvidenceValidationException("Evidence bundle contains no evidence items");
        }
        if (!bundle.repositoryId().equals(bundle.repositoryInformation().repositoryId())) {
            throw new EvidenceValidationException("Repository mismatch in bundle header");
        }
        if (!bundle.investigationId().equals(bundle.investigationTarget().investigationId())) {
            throw new EvidenceValidationException("Investigation mismatch in bundle header");
        }
        for (EvidenceRecord record : bundle.allEvidence()) {
            if (record.verificationStatus() != EvidenceVerificationStatus.VERIFIED) {
                throw new EvidenceValidationException(
                        "Unverified evidence present: " + record.evidenceId());
            }
            if (!record.repositoryId().equals(bundle.repositoryId())) {
                throw new EvidenceValidationException(
                        "Repository mismatch for evidence " + record.evidenceId());
            }
            if (!record.investigationId().equals(bundle.investigationId())) {
                throw new EvidenceValidationException(
                        "Investigation mismatch for evidence " + record.evidenceId());
            }
        }
    }

    private List<String> validateRecord(
            EvidenceRecord record, UUID repositoryId, UUID investigationId, Instant now) {
        List<String> errors = new ArrayList<>();
        if (record.evidenceId() == null) {
            errors.add("missing evidenceId");
        }
        if (record.evidenceType() == null) {
            errors.add("missing evidenceType");
        }
        if (record.source() == null) {
            errors.add("missing source/provenance");
        }
        if (record.sourceIdentifier() == null || record.sourceIdentifier().isBlank()) {
            errors.add("missing sourceIdentifier");
        }
        if (record.description() == null || record.description().isBlank()) {
            errors.add("missing description");
        }
        if (record.timestamp() == null) {
            errors.add("missing timestamp");
        } else if (record.timestamp().isAfter(now.plusSeconds(60))) {
            errors.add("invalid future timestamp");
        }
        if (!record.repositoryId().equals(repositoryId)) {
            errors.add("repository mismatch");
        }
        if (!record.investigationId().equals(investigationId)) {
            errors.add("investigation mismatch");
        }
        if (record.source() != null) {
            int expected = EvidenceConfidenceRules.forProvenance(record.source());
            if (record.confidence() != expected) {
                errors.add(
                        "confidence mismatch for "
                                + record.source()
                                + " expected="
                                + expected
                                + " actual="
                                + record.confidence());
            }
            EvidenceProvenance.valueOf(record.source().name());
        }
        return errors;
    }
}
