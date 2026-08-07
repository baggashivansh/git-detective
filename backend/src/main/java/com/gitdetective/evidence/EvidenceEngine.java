package com.gitdetective.evidence;

import com.gitdetective.evidence.dto.EvidenceBundleRequest;
import com.gitdetective.evidence.dto.EvidenceBundleView;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.evidence.service.EvidenceBundleService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Evidence Engine facade — single entry point for future AI services.
 *
 * <p>Gathers deterministic evidence, normalizes investigation outputs, eliminates duplicates,
 * assigns confidence, tracks provenance, and prepares immutable {@link EvidenceBundle} instances.
 *
 * <p>Performs no AI reasoning. Not exposed via public HTTP endpoints.
 */
@Component
@RequiredArgsConstructor
public class EvidenceEngine {

    private final EvidenceBundleService evidenceBundleService;

    /** Build (or return cached) evidence bundle for a completed investigation. */
    public EvidenceBundle gather(UUID investigationId) {
        return evidenceBundleService.build(new EvidenceBundleRequest(investigationId));
    }

    /** Force rebuild, bypassing the in-memory cache. */
    public EvidenceBundle gatherFresh(UUID investigationId) {
        return evidenceBundleService.build(new EvidenceBundleRequest(investigationId, true));
    }

    /** Stable internal view for future AI context assembly. */
    public EvidenceBundleView prepareForAi(UUID investigationId) {
        return evidenceBundleService.view(investigationId);
    }

    public void invalidate(UUID investigationId) {
        evidenceBundleService.invalidate(investigationId);
    }
}
