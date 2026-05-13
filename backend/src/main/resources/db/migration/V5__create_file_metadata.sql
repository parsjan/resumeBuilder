-- V5: file_metadata table
-- Tracks every file uploaded or exported by a user.
-- resume_id is nullable — a file may be a standalone upload not yet linked to a resume.

CREATE TABLE file_metadata (
    id                 UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id            UUID         NOT NULL,
    resume_id          UUID,                        -- nullable
    file_type          VARCHAR(20)  NOT NULL,
    file_url           VARCHAR(1000) NOT NULL,       -- S3 object key
    original_file_name VARCHAR(500),
    file_size_bytes    BIGINT,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    created_by         VARCHAR(255),
    updated_by         VARCHAR(255),

    CONSTRAINT pk_file_metadata        PRIMARY KEY (id),
    CONSTRAINT fk_file_metadata_user   FOREIGN KEY (user_id)   REFERENCES users   (id) ON DELETE CASCADE,
    CONSTRAINT fk_file_metadata_resume FOREIGN KEY (resume_id) REFERENCES resumes (id) ON DELETE SET NULL,
    CONSTRAINT chk_file_type           CHECK (file_type IN ('PDF', 'DOCX', 'DOC', 'IMAGE'))
);

CREATE INDEX idx_file_metadata_user_id   ON file_metadata (user_id);
CREATE INDEX idx_file_metadata_resume_id ON file_metadata (resume_id);
