package com.gitdetective.evidence.model;

/**
 * Normalized evidence categories used inside Evidence Bundles.
 *
 * <p>Independent of persistence {@code EvidenceType} so the evidence layer stays an abstraction.
 */
public enum EvidenceCategory {
    COMMIT,
    FILE,
    CLASS,
    METHOD,
    PACKAGE,
    CONTRIBUTOR,
    DEPENDENCY,
    CONFIG,
    ANNOTATION,
    IMPORT,
    RELATIONSHIP,
    TIMELINE,
    OWNERSHIP,
    IMPACT,
    HOTSPOT,
    PACKAGE_HEALTH,
    TRACE,
    CLUSTER,
    STATISTIC,
    TARGET,
    REPOSITORY
}
