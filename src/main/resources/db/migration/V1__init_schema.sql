-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================
-- 1. users
-- ============================================================
CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    username   VARCHAR(50)  UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password)
VALUES ('admin', '$2b$10$P6BNBTxikNRsMc3LmcM5W.q9xSuRGpCKpn5sS3q6OrQb.W5sVEoX6');

-- ============================================================
-- 2. knowledge_document
-- ============================================================
CREATE TABLE knowledge_document (
    id         BIGSERIAL    PRIMARY KEY,
    file_name  VARCHAR(255) NOT NULL,
    file_type  VARCHAR(50)  NOT NULL,
    file_size  BIGINT       NOT NULL,
    status     VARCHAR(20)  DEFAULT 'PENDING',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 3. knowledge_chunk
-- ============================================================
CREATE TABLE knowledge_chunk (
    id          BIGSERIAL    PRIMARY KEY,
    document_id BIGINT       NOT NULL REFERENCES knowledge_document(id),
    chunk_index INTEGER      NOT NULL,
    content     TEXT         NOT NULL,
    embedding   vector(1024),
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chunk_embedding
    ON knowledge_chunk USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);

CREATE INDEX idx_chunk_document_id ON knowledge_chunk (document_id);

-- ============================================================
-- 4. chat_session
-- ============================================================
CREATE TABLE chat_session (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    title      VARCHAR(255),
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_session_user_id ON chat_session (user_id);

-- ============================================================
-- 5. chat_message
-- ============================================================
CREATE TABLE chat_message (
    id         BIGSERIAL    PRIMARY KEY,
    session_id BIGINT       NOT NULL REFERENCES chat_session(id),
    role       VARCHAR(20)  NOT NULL,
    content    TEXT         NOT NULL,
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_session_id ON chat_message (session_id);
