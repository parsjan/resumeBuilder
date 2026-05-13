package com.app.file.service;

import com.app.file.dto.FileUploadResponse;
import com.app.file.dto.GeneratePdfRequest;
import com.app.file.dto.GeneratePdfResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileService {

    /**
     * Validate, upload to S3, persist metadata, and return a signed download URL.
     *
     * @param userId   owner of the file
     * @param resumeId optional — links the file to an existing resume
     * @param file     multipart upload
     */
    FileUploadResponse uploadFile(UUID userId, UUID resumeId, MultipartFile file);

    /**
     * Generate a PDF for the latest version of a resume, upload it to S3,
     * persist metadata, and return a signed download URL.
     */
    GeneratePdfResponse generatePdf(UUID userId, GeneratePdfRequest request);

    /**
     * Generate a fresh pre-signed GET URL for an existing S3 object key.
     * Use when a previously issued URL has expired.
     */
    String generateSignedUrl(String s3Key);

    /**
     * Delete the S3 object and remove the metadata row.
     *
     * @param fileId the {@link com.app.file.model.FileMetadata} primary key
     * @param userId ensures the file belongs to the requesting user
     */
    void deleteFile(UUID fileId, UUID userId);
}
