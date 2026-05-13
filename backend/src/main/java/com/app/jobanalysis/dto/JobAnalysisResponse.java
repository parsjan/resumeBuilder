package com.app.jobanalysis.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class JobAnalysisResponse {

    private UUID id;
    private String jobTitle;
    private String companyName;
    private List<String> keywords;
    private Integer matchScore;
    private Instant createdAt;
}
