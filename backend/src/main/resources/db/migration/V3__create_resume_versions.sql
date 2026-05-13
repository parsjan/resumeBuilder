-- V3: resume_versions table
-- Immutable snapshots; each save increments version_no within the parent resume.

CREATE TABLE resume_versions (
    id         UUID    NOT NULL DEFAULT gen_random_uuid(),
    resume_id  UUID    NOT NULL,
    content    JSONB   NOT NULL,
    version_no INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),

    CONSTRAINT pk_resume_versions        PRIMARY KEY (id),
    CONSTRAINT fk_resume_versions_resume FOREIGN KEY (resume_id) REFERENCES resumes (id) ON DELETE CASCADE,
    CONSTRAINT uq_resume_versions_no     UNIQUE (resume_id, version_no)
);

CREATE INDEX idx_resume_versions_resume_id  ON resume_versions (resume_id);
CREATE INDEX idx_resume_versions_version_no ON resume_versions (resume_id, version_no);
