package com.app.resume.model;

import com.app.common.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.Map;

/**
 * Immutable snapshot of a resume at a point in time.
 * Each save/publish action creates a new version row; existing rows are never mutated.
 * The latest version is the one with the highest {@code versionNo}.
 */
@Entity
@Table(
    name = "resume_versions",
    indexes = {
        @Index(name = "idx_resume_versions_resume_id",   columnList = "resume_id"),
        @Index(name = "idx_resume_versions_version_no",  columnList = "resume_id, version_no")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumeVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    /**
     * Full resume content as JSONB.
     * Flexible schema — sections (summary, experience, education, skills, etc.) are
     * stored as a nested map and interpreted by the frontend renderer.
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> content;

    /**
     * Monotonically increasing counter scoped to the parent resume.
     * Version 1 is the initial draft; incremented on each subsequent save.
     */
    @Column(name = "version_no", nullable = false)
    private Integer versionNo;
}
