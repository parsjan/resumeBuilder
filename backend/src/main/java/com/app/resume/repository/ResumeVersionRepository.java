package com.app.resume.repository;

import com.app.resume.model.ResumeVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeVersionRepository extends JpaRepository<ResumeVersion, UUID> {

    /** All versions for a resume, newest first. */
    List<ResumeVersion> findByResumeIdOrderByVersionNoDesc(UUID resumeId);

    /** Latest (highest) version number for a resume; returns 0 if none exist yet. */
    @Query("SELECT COALESCE(MAX(rv.versionNo), 0) FROM ResumeVersion rv WHERE rv.resume.id = :resumeId")
    int findMaxVersionNoByResumeId(@Param("resumeId") UUID resumeId);

    /** Fetch a specific version of a resume. */
    Optional<ResumeVersion> findByResumeIdAndVersionNo(UUID resumeId, int versionNo);

    /** Latest single version for a resume. */
    Optional<ResumeVersion> findFirstByResumeIdOrderByVersionNoDesc(UUID resumeId);

    /**
     * Fetch the latest version for each resume in {@code resumeIds} in one query.
     * Used by the list endpoint to avoid N+1 selects when paginating resumes.
     */
    @Query("""
            SELECT rv FROM ResumeVersion rv
            WHERE rv.resume.id IN :resumeIds
              AND rv.versionNo = (
                  SELECT MAX(rv2.versionNo) FROM ResumeVersion rv2
                  WHERE rv2.resume.id = rv.resume.id
              )
            """)
    List<ResumeVersion> findLatestForResumes(@Param("resumeIds") Collection<UUID> resumeIds);
}
