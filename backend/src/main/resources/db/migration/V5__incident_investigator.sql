ALTER TABLE investigations
    ADD COLUMN question TEXT,
    ADD COLUMN incident_report TEXT;

CREATE INDEX idx_investigations_question
    ON investigations (repository_id, created_at DESC)
    WHERE question IS NOT NULL;
