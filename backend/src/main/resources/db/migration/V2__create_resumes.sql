-- V2: resumes table
-- Soft-delete pattern — rows are never hard-deleted; is_deleted flags removal.

CREATE TABLE resumes (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL,
    title      VARCHAR(200) NOT NULL,
    template   VARCHAR(100),
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT pk_resumes      PRIMARY KEY (id),
    CONSTRAINT fk_resumes_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_resumes_user_id   ON resumes (user_id);
CREATE INDEX idx_resumes_is_deleted ON resumes (is_deleted);
