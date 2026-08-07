package com.gitdetective.evidence.dto;

import java.util.UUID;

/** Internal request to materialize an evidence bundle for an investigation. */
public record EvidenceBundleRequest(UUID investigationId, boolean bypassCache) {

    public EvidenceBundleRequest(UUID investigationId) {
        this(investigationId, false);
    }
}
