package com.app.jobanalysis.model;

import com.app.common.BaseEntity;
import com.app.resume.model.Resume;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;

/**
 * Result of analysing a job description against a specific resume.
 * {@code suggestions} is a JSONB map produced by the AI service, e.g.:
 * <pre>
 * {
 *   "missingKeywords": ["Kubernetes", "CI/CD"],
 *   "summaryTip": "Add quantified impact to the first bullet",
 *   "skillsGap": [...]
 * }
 * </pre>
 */
@Entity
@Table(
    name = "job_analyses",
    indexes = {
        @Index(name = "idx_job_analyses_resume_id", columnList = "resume_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAnalysis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "job_description", columnDefinition = "TEXT", nullable = false)
    private String jobDescription;

    /** Percentage (0–100) representing how well the resume matches the job description. */
    @Column(name = "match_score")
    private Integer matchScore;

    /** Structured AI suggestions stored as JSONB for schema flexibility. */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> suggestions;
}
