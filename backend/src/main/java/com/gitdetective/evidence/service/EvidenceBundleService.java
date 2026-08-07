package com.gitdetective.evidence.service;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.InvestigationStatus;
import com.gitdetective.evidence.builder.EvidenceBundleBuilder;
import com.gitdetective.evidence.cache.EvidenceBundleCache;
import com.gitdetective.evidence.dto.EvidenceBundleRequest;
import com.gitdetective.evidence.dto.EvidenceBundleView;
import com.gitdetective.evidence.model.EvidenceBundle;
import com.gitdetective.exception.RepositoryAnalysisException;
import com.gitdetective.exception.ResourceNotFoundException;
import com.gitdetective.investigation.InvestigationService;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Internal application service that materializes evidence bundles from completed investigations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvidenceBundleService {

    private final InvestigationService investigationService;
    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final EvidenceBundleBuilder evidenceBundleBuilder;
    private final EvidenceBundleCache evidenceBundleCache;

    @Transactional(readOnly = true)
    public EvidenceBundle build(EvidenceBundleRequest request) {
        UUID investigationId = request.investigationId();
        if (!request.bypassCache()) {
            var cached = evidenceBundleCache.get(investigationId);
            if (cached.isPresent()) {
                log.debug("Evidence bundle cache hit investigationId={}", investigationId);
                return cached.get().asCachedHit();
            }
        }

        InvestigationDetailResponse detail = investigationService.get(investigationId);
        if (detail.summary().status() != InvestigationStatus.COMPLETED) {
            throw new RepositoryAnalysisException(
                    HttpStatus.CONFLICT,
                    "INVESTIGATION_NOT_READY",
                    "Investigation must be COMPLETED before building an evidence bundle");
        }

        CodeRepository repository =
                codeRepositoryJpaRepository
                        .findById(detail.summary().repositoryId())
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Repository not found: "
                                                        + detail.summary().repositoryId()));

        EvidenceBundle bundle = evidenceBundleBuilder.build(detail, repository, false);
        evidenceBundleCache.put(investigationId, bundle);
        log.info(
                "Evidence bundle generated investigationId={} items={}",
                investigationId,
                bundle.allEvidence().size());
        return bundle;
    }

    @Transactional(readOnly = true)
    public EvidenceBundleView view(UUID investigationId) {
        return EvidenceBundleView.from(build(new EvidenceBundleRequest(investigationId)));
    }

    public void invalidate(UUID investigationId) {
        evidenceBundleCache.invalidate(investigationId);
    }
}
