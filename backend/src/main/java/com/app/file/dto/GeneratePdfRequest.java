package com.app.file.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class GeneratePdfRequest {

    @NotNull(message = "Resume ID is required")
    private UUID resumeId;
}
