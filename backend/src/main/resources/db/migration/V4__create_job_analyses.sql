-- V4: job_analyses table
-- Stores AI-generated analysis of a job description against a specific resume.

CREATE TABLE job_analyses (
    id              UUID    NOT NULL DEFAULT gen_random_uuid(),
    resume_id       UUID    NOT NULL,
    job_description TEXT    NOT NULL,
    match_score     INTEGER,              -- 0–100; NULL until analysis completes
    suggestions     JSONB,               -- AI-generated structured suggestions
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),

    CONSTRAINT pk_job_analyses        PRIMARY KEY (id),
    CONSTRAINT fk_job_analyses_resume FOREIGN KEY (resume_id) REFERENCES resumes (id) ON DELETE CASCADE,
    CONSTRAINT chk_match_score        CHECK (match_score IS NULL OR (match_score >= 0 AND match_score <= 100))
);

CREATE INDEX idx_job_analyses_resume_id ON job_analyses (resume_id);
