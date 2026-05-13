package com.app.jobanalysis.repository;

import com.app.jobanalysis.model.JobAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobAnalysisRepository extends JpaRepository<JobAnalysis, UUID> {

    /** All analyses for a resume, newest first. */
    List<JobAnalysis> findByResumeIdOrderByCreatedAtDesc(UUID resumeId);

    /** All analyses across all resumes owned by a user. */
    List<JobAnalysis> findByResumeUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<JobAnalysis> findByIdAndResumeId(UUID id, UUID resumeId);

    /** Scoped lookup — ensures the analysis belongs to a resume owned by the requesting user. */
    Optional<JobAnalysis> findByIdAndResumeUserId(UUID id, UUID userId);
}
