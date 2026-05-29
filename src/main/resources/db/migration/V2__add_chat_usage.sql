-- ============================================================
-- chat_usage: tracks token consumption per request
-- ============================================================
CREATE TABLE chat_usage (
    id                BIGSERIAL    PRIMARY KEY,
    session_id        BIGINT       NOT NULL REFERENCES chat_session(id),
    model_name        VARCHAR(100) NOT NULL,
    prompt_tokens     INTEGER      NOT NULL DEFAULT 0,
    completion_tokens INTEGER      NOT NULL DEFAULT 0,
    total_tokens      INTEGER      NOT NULL DEFAULT 0,
    created_at        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usage_session_id ON chat_usage (session_id);
