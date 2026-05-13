package com.app.file.model;

import com.app.common.BaseEntity;
import com.app.resume.model.Resume;
import com.app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Tracks every file uploaded or exported by a user.
 * {@code resume} is nullable — a file may be a standalone upload (e.g. imported PDF)
 * that has not yet been linked to a resume.
 */
@Entity
@Table(
    name = "file_metadata",
    indexes = {
        @Index(name = "idx_file_metadata_user_id",   columnList = "user_id"),
        @Index(name = "idx_file_metadata_resume_id", columnList = "resume_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileMetadata extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Nullable — a file is not always associated with a specific resume. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private FileType fileType;

    /** S3 object key (e.g. {@code uploads/uuid/timestamp_file.pdf}). Used to generate signed URLs. */
    @Column(name = "file_url", nullable = false, length = 1000)
    private String s3Key;

    /** Original filename provided by the user at upload time. */
    @Column(name = "original_file_name", length = 500)
    private String originalFileName;

    /** File size in bytes. */
    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    // ── Enum ──────────────────────────────────────────────────────────────

    public enum FileType {
        PDF, DOCX, DOC, IMAGE
    }
}
