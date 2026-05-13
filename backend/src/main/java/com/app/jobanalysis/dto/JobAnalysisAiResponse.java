package com.app.jobanalysis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** Internal DTO — maps the raw JSON returned by the AI job-analysis prompt. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobAnalysisAiResponse {

    /** 0–100: how well the resume matches the job description. */
    private int matchScore;

    /** Key terms extracted from the job description that are present in the resume. */
    private List<String> keywords;

    /** Important terms in the job description that are absent from the resume. */
    private List<String> missingKeywords;

    /** Actionable improvement tips. */
    private List<String> tips;
}
