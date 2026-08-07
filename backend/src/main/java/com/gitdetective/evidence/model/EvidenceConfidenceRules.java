package com.gitdetective.evidence.model;

/**
 * Deterministic confidence percentages for evidence provenance.
 *
 * <p>No AI scoring. Values are fixed by provenance class and documented in {@code
 * docs/EVIDENCE_ENGINE.md}.
 */
public final class EvidenceConfidenceRules {

    public static final int DIRECT_GIT_HISTORY = 100;
    public static final int STATIC_PARSER = 100;
    public static final int REPOSITORY_METADATA = 100;
    public static final int RELATIONSHIP_GRAPH = 95;
    public static final int DERIVED_ENGINE_GRAPH = 95;
    public static final int DERIVED_ENGINE_GIT = 100;

    private EvidenceConfidenceRules() {}

    public static int forProvenance(EvidenceProvenance provenance) {
        return switch (provenance) {
            case GIT_COMMIT, TIMELINE_ENGINE, OWNERSHIP_ENGINE, FILE_HISTORY_ENGINE ->
                    DIRECT_GIT_HISTORY;
            case JAVA_PARSER, WORKSPACE_SCAN -> STATIC_PARSER;
            case REPOSITORY_METADATA, INVESTIGATION_TARGET, STATISTICS_COLLECTOR ->
                    REPOSITORY_METADATA;
            case DEPENDENCY_GRAPH,
                    RELATIONSHIP_ENGINE,
                    IMPACT_ENGINE,
                    PACKAGE_HEALTH_ENGINE,
                    HOTSPOT_DETECTOR,
                    REQUEST_TRACE_ENGINE,
                    AUTH_FLOW_DETECTOR,
                    COMMIT_CLUSTERING_ENGINE ->
                    RELATIONSHIP_GRAPH;
            case INVESTIGATION_EVIDENCE -> DERIVED_ENGINE_GIT;
        };
    }
}
