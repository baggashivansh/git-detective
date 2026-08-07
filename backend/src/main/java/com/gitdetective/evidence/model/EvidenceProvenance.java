package com.gitdetective.evidence.model;

/**
 * Deterministic provenance for an evidence item. Enables full auditability of the source engine or
 * metadata system that produced the fact.
 */
public enum EvidenceProvenance {
    GIT_COMMIT,
    JAVA_PARSER,
    DEPENDENCY_GRAPH,
    REPOSITORY_METADATA,
    WORKSPACE_SCAN,
    TIMELINE_ENGINE,
    OWNERSHIP_ENGINE,
    IMPACT_ENGINE,
    PACKAGE_HEALTH_ENGINE,
    HOTSPOT_DETECTOR,
    RELATIONSHIP_ENGINE,
    STATISTICS_COLLECTOR,
    FILE_HISTORY_ENGINE,
    REQUEST_TRACE_ENGINE,
    AUTH_FLOW_DETECTOR,
    COMMIT_CLUSTERING_ENGINE,
    INVESTIGATION_TARGET,
    INVESTIGATION_EVIDENCE
}
