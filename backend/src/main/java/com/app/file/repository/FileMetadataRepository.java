package com.app.file.repository;

import com.app.file.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<FileMetadata> findByResumeId(UUID resumeId);

    Optional<FileMetadata> findByIdAndUserId(UUID id, UUID userId);

    void deleteByResumeId(UUID resumeId);
}
