-- Phase 5: production query indexes for investigation and parser lookups

CREATE INDEX IF NOT EXISTS idx_investigation_hotspots_investigation
    ON investigation_hotspots (investigation_id);

CREATE INDEX IF NOT EXISTS idx_investigation_package_health_investigation
    ON investigation_package_health (investigation_id);

CREATE INDEX IF NOT EXISTS idx_investigation_commit_clusters_investigation
    ON investigation_commit_clusters (investigation_id);

CREATE INDEX IF NOT EXISTS idx_investigation_traces_investigation
    ON investigation_traces (investigation_id);

CREATE INDEX IF NOT EXISTS idx_fields_type
    ON fields (type_id);

CREATE INDEX IF NOT EXISTS idx_file_imports_file
    ON file_imports (file_id);

CREATE INDEX IF NOT EXISTS idx_file_exports_file
    ON file_exports (file_id);

CREATE INDEX IF NOT EXISTS idx_workspace_metadata_session
    ON workspace_metadata (analysis_session_id);

CREATE INDEX IF NOT EXISTS idx_code_types_file
    ON code_types (file_id);

CREATE INDEX IF NOT EXISTS idx_code_types_package
    ON code_types (package_id);

CREATE INDEX IF NOT EXISTS idx_investigations_repository
    ON investigations (repository_id);
