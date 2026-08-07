-- Phase 4: Intelligent Investigation Assistant (conversation memory)

CREATE TABLE assistant_conversations (
    id                  UUID PRIMARY KEY,
    repository_id       UUID NOT NULL,
    investigation_id    UUID NOT NULL,
    title               VARCHAR(512),
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_assistant_conversations_investigation
        FOREIGN KEY (investigation_id) REFERENCES investigations (id) ON DELETE CASCADE
);

CREATE INDEX idx_assistant_conversations_investigation
    ON assistant_conversations (investigation_id);

CREATE INDEX idx_assistant_conversations_repository
    ON assistant_conversations (repository_id);

CREATE TABLE assistant_messages (
    id                  UUID PRIMARY KEY,
    conversation_id     UUID NOT NULL,
    role                VARCHAR(32) NOT NULL,
    content             TEXT NOT NULL,
    intent              VARCHAR(64),
    response_payload    TEXT,
    confidence          INTEGER,
    evidence_ids_json   TEXT,
    created_at          TIMESTAMPTZ NOT NULL,
    sort_order          INTEGER NOT NULL,
    CONSTRAINT fk_assistant_messages_conversation
        FOREIGN KEY (conversation_id) REFERENCES assistant_conversations (id) ON DELETE CASCADE
);

CREATE INDEX idx_assistant_messages_conversation
    ON assistant_messages (conversation_id, sort_order);
