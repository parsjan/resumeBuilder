package com.app.resume.repository;

import com.app.resume.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    /** Active (non-deleted) resumes for a user, newest first. */
    Page<Resume> findByUserIdAndIsDeletedFalse(UUID userId, Pageable pageable);

    /** Find an active resume by id scoped to a user — prevents cross-user access. */
    Optional<Resume> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    /** Count of active resumes for a user (used for plan limit checks). */
    long countByUserIdAndIsDeletedFalse(UUID userId);

    /** Soft-delete without loading the entity into memory. */
    @Modifying
    @Query("UPDATE Resume r SET r.isDeleted = true WHERE r.id = :id AND r.user.id = :userId")
    int softDelete(@Param("id") UUID id, @Param("userId") UUID userId);
}
