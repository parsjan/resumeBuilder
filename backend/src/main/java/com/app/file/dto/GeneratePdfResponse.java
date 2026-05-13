package com.app.file.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class GeneratePdfResponse {

    private UUID fileId;
    private UUID resumeId;
    private String fileName;

    /**
     * Pre-signed S3 URL for downloading the generated PDF.
     * Valid for {@code app.aws.s3.signed-url-expiration-minutes}.
     */
    private String signedUrl;

    private Instant createdAt;
}
