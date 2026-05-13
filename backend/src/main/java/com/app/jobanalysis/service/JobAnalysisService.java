package com.app.jobanalysis.service;

import com.app.jobanalysis.dto.JobAnalysisRequest;
import com.app.jobanalysis.dto.JobAnalysisResponse;

import java.util.List;
import java.util.UUID;

public interface JobAnalysisService {

    JobAnalysisResponse analyze(UUID userId, JobAnalysisRequest request);

    List<JobAnalysisResponse> listByUser(UUID userId);

    JobAnalysisResponse getById(UUID userId, UUID analysisId);

    void delete(UUID userId, UUID analysisId);
}
