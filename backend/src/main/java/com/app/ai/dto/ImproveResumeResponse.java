package com.app.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImproveResumeResponse {

    /** Improved resume sections — deserialized from the AI's "improvedContent" key, serialized as "sections". */
    @JsonAlias("improvedContent")
    private Map<String, Object> sections;

    /** Human-readable list of changes made and their reasoning. */
    private List<String> changes;

    /** Estimated ATS match score for the target role (0–100). */
    private int atsScore;

    /** Keywords extracted / added for ATS optimisation. */
    private List<String> keywords;
}
