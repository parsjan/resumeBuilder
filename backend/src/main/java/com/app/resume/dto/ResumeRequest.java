package com.app.resume.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class ResumeRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 100, message = "Template must not exceed 100 characters")
    private String template;

    /**
     * Full resume sections to be stored as a new {@link com.app.resume.model.ResumeVersion}.
     * Optional on creation — defaults to an empty map so a blank resume can be created with just a title.
     */
    private Map<String, Object> sections;
}
