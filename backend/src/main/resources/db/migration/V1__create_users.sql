-- V1: users table
-- Supports both local (email/password) and OAuth2 (Google) sign-in.

CREATE TABLE users (
    id           UUID        NOT NULL DEFAULT gen_random_uuid(),
    email        VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255),                    -- NULL for OAuth2 users
    provider     VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    provider_id  VARCHAR(255),                     -- NULL for LOCAL users
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    created_by   VARCHAR(255),
    updated_by   VARCHAR(255),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_provider CHECK (provider IN ('LOCAL', 'GOOGLE'))
);

CREATE INDEX idx_users_email       ON users (email);
CREATE INDEX idx_users_provider_id ON users (provider_id);
