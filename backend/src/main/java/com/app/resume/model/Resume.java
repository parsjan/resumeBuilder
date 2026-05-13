package com.app.resume.model;

import com.app.common.BaseEntity;
import com.app.file.model.FileMetadata;
import com.app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A resume belongs to one user and can have multiple versioned snapshots.
 * Soft-delete is handled via {@code isDeleted}; never hard-delete rows.
 */
@Entity
@Table(
    name = "resumes",
    indexes = {
        @Index(name = "idx_resumes_user_id",    columnList = "user_id"),
        @Index(name = "idx_resumes_is_deleted",  columnList = "is_deleted")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    /** Template name/identifier used to render this resume (e.g. "modern", "classic"). */
    @Column(length = 100)
    private String template;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    // ── Inverse relationships (for convenience; not part of the core schema) ──

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @OrderBy("versionNo DESC")
    private List<ResumeVersion> versions = new ArrayList<>();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FileMetadata> files = new ArrayList<>();
}
