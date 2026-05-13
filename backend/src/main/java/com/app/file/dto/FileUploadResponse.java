package com.app.file.dto;

import com.app.file.model.FileMetadata;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class FileUploadResponse {

    private UUID fileId;
    private String originalFileName;
    private FileMetadata.FileType fileType;
    private Long fileSizeBytes;

    /**
     * Pre-signed S3 URL valid for {@code app.aws.s3.signed-url-expiration-minutes}.
     * Use this URL directly to download the file from S3 without additional auth.
     */
    private String signedUrl;

    private Instant createdAt;
}
