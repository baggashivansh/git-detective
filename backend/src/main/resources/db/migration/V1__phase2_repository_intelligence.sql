CREATE TABLE repositories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    source_uri TEXT NOT NULL,
    remote_url TEXT,
    default_branch VARCHAR(255),
    total_commits BIGINT NOT NULL DEFAULT 0,
    size_bytes BIGINT NOT NULL DEFAULT 0,
    primary_language VARCHAR(64),
    status VARCHAR(32) NOT NULL,
    status_message VARCHAR(512),
    progress_percent INTEGER NOT NULL DEFAULT 0,
    error_code VARCHAR(64),
    error_message TEXT,
    latest_commit_sha VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    analyzed_at TIMESTAMPTZ,
    CONSTRAINT uq_repositories_source UNIQUE (source_type, source_uri)
);

CREATE TABLE analysis_sessions (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    duration_ms BIGINT,
    error_code VARCHAR(64),
    error_message TEXT,
    workspace_path TEXT
);

CREATE INDEX idx_analysis_sessions_repository_id ON analysis_sessions (repository_id);

CREATE TABLE workspace_metadata (
    id UUID PRIMARY KEY,
    analysis_session_id UUID NOT NULL REFERENCES analysis_sessions (id) ON DELETE CASCADE,
    workspace_key VARCHAR(512) NOT NULL,
    path TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    cleaned_at TIMESTAMPTZ,
    CONSTRAINT uq_workspace_key UNIQUE (workspace_key)
);

CREATE TABLE branches (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    head_commit_sha VARCHAR(64),
    CONSTRAINT uq_branch_repo_name UNIQUE (repository_id, name)
);

CREATE TABLE tags (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    commit_sha VARCHAR(64),
    CONSTRAINT uq_tag_repo_name UNIQUE (repository_id, name)
);

CREATE TABLE commits (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    sha VARCHAR(64) NOT NULL,
    author_name VARCHAR(255) NOT NULL,
    author_email VARCHAR(320) NOT NULL,
    authored_at TIMESTAMPTZ NOT NULL,
    message TEXT NOT NULL,
    is_merge BOOLEAN NOT NULL DEFAULT FALSE,
    insertions INTEGER NOT NULL DEFAULT 0,
    deletions INTEGER NOT NULL DEFAULT 0,
    files_changed_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_commit_repo_sha UNIQUE (repository_id, sha)
);

CREATE INDEX idx_commits_repository_authored_at ON commits (repository_id, authored_at DESC);
CREATE INDEX idx_commits_author_email ON commits (repository_id, author_email);

CREATE TABLE commit_parents (
    commit_id UUID NOT NULL REFERENCES commits (id) ON DELETE CASCADE,
    parent_sha VARCHAR(64) NOT NULL,
    parent_order INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (commit_id, parent_sha)
);

CREATE TABLE commit_branches (
    commit_id UUID NOT NULL REFERENCES commits (id) ON DELETE CASCADE,
    branch_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (commit_id, branch_name)
);

CREATE TABLE commit_tags (
    commit_id UUID NOT NULL REFERENCES commits (id) ON DELETE CASCADE,
    tag_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (commit_id, tag_name)
);

CREATE TABLE commit_file_changes (
    id UUID PRIMARY KEY,
    commit_id UUID NOT NULL REFERENCES commits (id) ON DELETE CASCADE,
    path TEXT NOT NULL,
    change_type VARCHAR(32) NOT NULL,
    insertions INTEGER NOT NULL DEFAULT 0,
    deletions INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_commit_file_changes_commit_id ON commit_file_changes (commit_id);

CREATE TABLE contributors (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(320) NOT NULL,
    commit_count BIGINT NOT NULL DEFAULT 0,
    files_modified BIGINT NOT NULL DEFAULT 0,
    lines_added BIGINT NOT NULL DEFAULT 0,
    lines_deleted BIGINT NOT NULL DEFAULT 0,
    last_contribution_at TIMESTAMPTZ,
    contribution_percentage NUMERIC(6, 3) NOT NULL DEFAULT 0,
    CONSTRAINT uq_contributor_repo_email UNIQUE (repository_id, email)
);

CREATE TABLE files (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    path TEXT NOT NULL,
    name VARCHAR(512) NOT NULL,
    parent_path TEXT,
    extension VARCHAR(64),
    language VARCHAR(64),
    size_bytes BIGINT NOT NULL DEFAULT 0,
    line_count INTEGER NOT NULL DEFAULT 0,
    is_directory BOOLEAN NOT NULL DEFAULT FALSE,
    is_binary BOOLEAN NOT NULL DEFAULT FALSE,
    is_hidden BOOLEAN NOT NULL DEFAULT FALSE,
    is_ignored BOOLEAN NOT NULL DEFAULT FALSE,
    content_hash VARCHAR(128),
    package_name VARCHAR(512),
    method_count INTEGER NOT NULL DEFAULT 0,
    field_count INTEGER NOT NULL DEFAULT 0,
    import_count INTEGER NOT NULL DEFAULT 0,
    export_count INTEGER NOT NULL DEFAULT 0,
    created_at_fs TIMESTAMPTZ,
    modified_at_fs TIMESTAMPTZ,
    CONSTRAINT uq_file_repo_path UNIQUE (repository_id, path)
);

CREATE INDEX idx_files_repository_language ON files (repository_id, language);
CREATE INDEX idx_files_repository_parent ON files (repository_id, parent_path);

CREATE TABLE packages (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    name VARCHAR(512) NOT NULL,
    path TEXT,
    file_count INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT uq_package_repo_name UNIQUE (repository_id, name)
);

CREATE TABLE code_types (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    package_id UUID REFERENCES packages (id) ON DELETE SET NULL,
    file_id UUID REFERENCES files (id) ON DELETE CASCADE,
    name VARCHAR(512) NOT NULL,
    fully_qualified_name VARCHAR(1024) NOT NULL,
    kind VARCHAR(32) NOT NULL,
    visibility VARCHAR(32),
    superclass_name VARCHAR(1024),
    start_line INTEGER,
    end_line INTEGER
);

CREATE INDEX idx_code_types_repository_name ON code_types (repository_id, name);
CREATE INDEX idx_code_types_fqn ON code_types (repository_id, fully_qualified_name);

CREATE TABLE type_interfaces (
    type_id UUID NOT NULL REFERENCES code_types (id) ON DELETE CASCADE,
    interface_name VARCHAR(1024) NOT NULL,
    PRIMARY KEY (type_id, interface_name)
);

CREATE TABLE methods (
    id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES code_types (id) ON DELETE CASCADE,
    name VARCHAR(512) NOT NULL,
    signature TEXT NOT NULL,
    return_type VARCHAR(1024),
    visibility VARCHAR(32),
    is_constructor BOOLEAN NOT NULL DEFAULT FALSE,
    parameter_count INTEGER NOT NULL DEFAULT 0,
    start_line INTEGER
);

CREATE INDEX idx_methods_type_id ON methods (type_id);

CREATE TABLE fields (
    id UUID PRIMARY KEY,
    type_id UUID NOT NULL REFERENCES code_types (id) ON DELETE CASCADE,
    name VARCHAR(512) NOT NULL,
    type_name VARCHAR(1024),
    visibility VARCHAR(32)
);

CREATE TABLE annotations (
    id UUID PRIMARY KEY,
    owner_kind VARCHAR(32) NOT NULL,
    owner_id UUID NOT NULL,
    name VARCHAR(1024) NOT NULL
);

CREATE INDEX idx_annotations_owner ON annotations (owner_kind, owner_id);

CREATE TABLE file_imports (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL REFERENCES files (id) ON DELETE CASCADE,
    import_name TEXT NOT NULL,
    is_static BOOLEAN NOT NULL DEFAULT FALSE,
    is_asterisk BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE file_exports (
    id UUID PRIMARY KEY,
    file_id UUID NOT NULL REFERENCES files (id) ON DELETE CASCADE,
    export_name VARCHAR(1024) NOT NULL
);

CREATE TABLE dependency_nodes (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    node_key VARCHAR(1024) NOT NULL,
    node_type VARCHAR(64) NOT NULL,
    label VARCHAR(1024) NOT NULL,
    CONSTRAINT uq_dependency_node UNIQUE (repository_id, node_key)
);

CREATE TABLE dependency_edges (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    source_node_id UUID NOT NULL REFERENCES dependency_nodes (id) ON DELETE CASCADE,
    target_node_id UUID NOT NULL REFERENCES dependency_nodes (id) ON DELETE CASCADE,
    relationship VARCHAR(64) NOT NULL,
    CONSTRAINT uq_dependency_edge UNIQUE (repository_id, source_node_id, target_node_id, relationship)
);

CREATE INDEX idx_dependency_edges_repository ON dependency_edges (repository_id);

CREATE TABLE language_statistics (
    id UUID PRIMARY KEY,
    repository_id UUID NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    language VARCHAR(64) NOT NULL,
    file_count INTEGER NOT NULL DEFAULT 0,
    line_count BIGINT NOT NULL DEFAULT 0,
    byte_count BIGINT NOT NULL DEFAULT 0,
    percentage NUMERIC(6, 3) NOT NULL DEFAULT 0,
    CONSTRAINT uq_language_stats UNIQUE (repository_id, language)
);

CREATE TABLE repository_statistics (
    repository_id UUID PRIMARY KEY REFERENCES repositories (id) ON DELETE CASCADE,
    total_files BIGINT NOT NULL DEFAULT 0,
    total_directories BIGINT NOT NULL DEFAULT 0,
    total_lines BIGINT NOT NULL DEFAULT 0,
    total_packages BIGINT NOT NULL DEFAULT 0,
    total_classes BIGINT NOT NULL DEFAULT 0,
    total_interfaces BIGINT NOT NULL DEFAULT 0,
    total_enums BIGINT NOT NULL DEFAULT 0,
    total_methods BIGINT NOT NULL DEFAULT 0,
    total_contributors BIGINT NOT NULL DEFAULT 0,
    total_branches BIGINT NOT NULL DEFAULT 0,
    total_tags BIGINT NOT NULL DEFAULT 0,
    binary_file_count BIGINT NOT NULL DEFAULT 0,
    ignored_file_count BIGINT NOT NULL DEFAULT 0
);
