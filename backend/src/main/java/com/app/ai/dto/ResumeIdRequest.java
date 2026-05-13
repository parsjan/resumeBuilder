package com.app.ai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/** Shared request DTO used by endpoints that operate on an existing resume. */
@Data
public class ResumeIdRequest {

    @NotNull(message = "resumeId is required")
    private UUID resumeId;
}
