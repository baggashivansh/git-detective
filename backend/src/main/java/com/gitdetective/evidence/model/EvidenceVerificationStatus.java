package com.gitdetective.evidence.model;

/** Verification status for a single evidence item. */
public enum EvidenceVerificationStatus {
    /** Item passed validation and references are consistent. */
    VERIFIED,
    /** Item collected but not yet validated. */
    PENDING,
    /** Item failed validation and must not enter a published bundle. */
    INVALID
}
