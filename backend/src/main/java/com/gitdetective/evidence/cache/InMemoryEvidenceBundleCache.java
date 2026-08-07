package com.gitdetective.evidence.cache;

import com.gitdetective.evidence.model.EvidenceBundle;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Process-local evidence bundle cache. Avoids recomputing identical investigations within a JVM.
 */
@Component
public class InMemoryEvidenceBundleCache implements EvidenceBundleCache {

    private final ConcurrentHashMap<UUID, EvidenceBundle> store = new ConcurrentHashMap<>();

    @Override
    public Optional<EvidenceBundle> get(UUID investigationId) {
        return Optional.ofNullable(store.get(investigationId));
    }

    @Override
    public void put(UUID investigationId, EvidenceBundle bundle) {
        store.put(investigationId, bundle);
    }

    @Override
    public void invalidate(UUID investigationId) {
        store.remove(investigationId);
    }

    @Override
    public void clear() {
        store.clear();
    }

    @Override
    public int size() {
        return store.size();
    }
}
