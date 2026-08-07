package com.gitdetective.evidence.collector;

import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.evidence.model.EvidenceRecord;
import java.util.List;

/** Single-responsibility collector that extracts evidence from an investigation detail. */
public interface EvidenceCollector {

    /** Stable collector name for provenance and diagnostics. */
    String name();

    /**
     * Collect evidence records for the investigation.
     *
     * @param detail investigation detail (never null)
     * @param repository repository header metadata (never null)
     */
    List<EvidenceRecord> collect(InvestigationDetailResponse detail, CodeRepository repository);
}
