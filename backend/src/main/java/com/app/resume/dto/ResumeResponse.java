package com.app.resume.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ResumeResponse {

    private UUID id;
    private String title;
    private String template;

    /** Version number of the snapshot included in {@code content}. Null if no version saved yet. */
    private Integer versionNo;

    /** Content from the latest {@link com.app.resume.model.ResumeVersion}. */
    private Map<String, Object> sections;

    private Instant createdAt;
    private Instant updatedAt;
}
