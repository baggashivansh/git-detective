package com.gitdetective.evidence.cache;

import com.gitdetective.evidence.model.EvidenceBundle;
import java.util.Optional;
import java.util.UUID;

/** Cache contract for evidence bundles. In-memory today; designed for future Redis adapters. */
public interface EvidenceBundleCache {

    Optional<EvidenceBundle> get(UUID investigationId);

    void put(UUID investigationId, EvidenceBundle bundle);

    void invalidate(UUID investigationId);

    void clear();

    int size();
}
