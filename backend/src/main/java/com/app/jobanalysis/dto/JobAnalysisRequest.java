package com.app.jobanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JobAnalysisRequest {

    private String jobTitle;
    private String companyName;

    @NotBlank
    private String jobDescription;

    /** Optional: compare against an existing resume. */
    private java.util.UUID resumeId;
}
