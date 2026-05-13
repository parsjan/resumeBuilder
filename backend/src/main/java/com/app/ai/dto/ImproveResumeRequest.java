package com.app.ai.dto;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class ImproveResumeRequest {

    /**
     * Optional — if provided, the service fetches the resume content from the DB
     * and uses it as the base for improvement (overrides {@code currentContent}).
     */
    private UUID resumeId;

    /**
     * Raw resume content map. Loaded automatically from the DB when {@code resumeId} is supplied;
     * required only when {@code resumeId} is omitted.
     */
    private Map<String, Object> currentContent;

    private String targetJobTitle;
    private String targetJobDescription;
}
