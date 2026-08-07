CREATE TABLE investigations (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    target_type VARCHAR(32) NOT NULL,
    target_ref VARCHAR(1024) NOT NULL,
    target_label VARCHAR(1024) NOT NULL,
    status VARCHAR(32) NOT NULL,
    summary TEXT,
    bus_factor_score INTEGER,
    bus_factor_level VARCHAR(16),
    blast_radius_score NUMERIC(8, 3),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE INDEX idx_investigations_repository_id ON investigations (repository_id);
CREATE INDEX idx_investigations_created_at ON investigations (created_at DESC);

CREATE TABLE investigation_evidence (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    evidence_type VARCHAR(64) NOT NULL,
    source_kind VARCHAR(64) NOT NULL,
    source_ref VARCHAR(1024) NOT NULL,
    label VARCHAR(1024) NOT NULL,
    detail TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_investigation_evidence_investigation_id
    ON investigation_evidence (investigation_id);

CREATE TABLE investigation_timeline_events (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    occurred_at TIMESTAMPTZ NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    title VARCHAR(1024) NOT NULL,
    detail TEXT,
    actor_name VARCHAR(255),
    actor_email VARCHAR(320),
    commit_sha VARCHAR(64),
    evidence_ref VARCHAR(1024),
    sort_order INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_investigation_timeline_investigation_id
    ON investigation_timeline_events (investigation_id, occurred_at);

CREATE TABLE investigation_relationships (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    source_key VARCHAR(1024) NOT NULL,
    source_label VARCHAR(1024) NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    target_key VARCHAR(1024) NOT NULL,
    target_label VARCHAR(1024) NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    relationship_type VARCHAR(64) NOT NULL,
    evidence_ref VARCHAR(1024)
);

CREATE INDEX idx_investigation_relationships_investigation_id
    ON investigation_relationships (investigation_id);

CREATE TABLE investigation_ownership (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    contributor_email VARCHAR(320) NOT NULL,
    contributor_name VARCHAR(255) NOT NULL,
    total_commits BIGINT NOT NULL DEFAULT 0,
    recent_commits BIGINT NOT NULL DEFAULT 0,
    lines_changed BIGINT NOT NULL DEFAULT 0,
    ownership_percentage NUMERIC(8, 3) NOT NULL DEFAULT 0,
    ownership_kind VARCHAR(32) NOT NULL,
    last_contribution_at TIMESTAMPTZ
);

CREATE INDEX idx_investigation_ownership_investigation_id
    ON investigation_ownership (investigation_id);

CREATE TABLE investigation_impact_items (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    item_kind VARCHAR(64) NOT NULL,
    item_ref VARCHAR(1024) NOT NULL,
    item_label VARCHAR(1024) NOT NULL,
    dependency_depth INTEGER NOT NULL DEFAULT 0,
    reason TEXT NOT NULL
);

CREATE INDEX idx_investigation_impact_investigation_id
    ON investigation_impact_items (investigation_id);

CREATE TABLE investigation_hotspots (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    hotspot_kind VARCHAR(64) NOT NULL,
    item_ref VARCHAR(1024) NOT NULL,
    item_label VARCHAR(1024) NOT NULL,
    score NUMERIC(12, 3) NOT NULL DEFAULT 0,
    rank_position INTEGER NOT NULL DEFAULT 0,
    detail TEXT
);

CREATE TABLE investigation_package_health (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    package_name VARCHAR(1024) NOT NULL,
    complexity_score NUMERIC(10, 3) NOT NULL DEFAULT 0,
    dependency_count INTEGER NOT NULL DEFAULT 0,
    package_size INTEGER NOT NULL DEFAULT 0,
    modification_frequency NUMERIC(10, 3) NOT NULL DEFAULT 0,
    contributor_count INTEGER NOT NULL DEFAULT 0,
    growth_score NUMERIC(10, 3) NOT NULL DEFAULT 0,
    risk_level VARCHAR(16) NOT NULL
);

CREATE TABLE investigation_commit_clusters (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    cluster_label VARCHAR(1024) NOT NULL,
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    commit_count INTEGER NOT NULL DEFAULT 0,
    shared_files INTEGER NOT NULL DEFAULT 0,
    contributors TEXT,
    commit_shas TEXT
);

CREATE TABLE investigation_traces (
    id UUID PRIMARY KEY,
    investigation_id UUID NOT NULL REFERENCES investigations (id) ON DELETE CASCADE,
    trace_kind VARCHAR(64) NOT NULL,
    step_order INTEGER NOT NULL,
    step_label VARCHAR(1024) NOT NULL,
    step_ref VARCHAR(1024),
    evidence_ref VARCHAR(1024),
    detail TEXT
);
