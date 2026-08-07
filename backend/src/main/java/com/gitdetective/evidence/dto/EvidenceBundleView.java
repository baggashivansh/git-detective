package com.gitdetective.evidence.dto;

import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.model.EvidenceRecord;
import com.gitdetective.evidence.model.EvidenceSections;
import java.util.List;

/**
 * Stable internal view of an evidence bundle for future AI services.
 *
 * <p>Does not expose investigation JPA entities.
 */
public record EvidenceBundleView(
        EvidenceBundle bundle,
        EvidenceSections.BundleMetadata metadata,
        EvidenceSections.EvidenceSummary summary,
        List<EvidenceRecord> evidence) {

    public static EvidenceBundleView from(EvidenceBundle bundle) {
        return new EvidenceBundleView(
                bundle, bundle.metadata(), bundle.evidenceSummary(), bundle.allEvidence());
    }
}
