package com.app.file.controller;

import com.app.common.ApiResponse;
import com.app.file.dto.FileUploadResponse;
import com.app.file.dto.GeneratePdfRequest;
import com.app.file.dto.GeneratePdfResponse;
import com.app.file.service.FileService;
import com.app.user.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * POST /files/generate-pdf
     *
     * Generates a PDF for the latest version of a resume, uploads it to S3,
     * persists metadata, and returns a pre-signed download URL.
     *
     * Body: { "resumeId": "uuid" }
     */
    @PostMapping("/generate-pdf")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<GeneratePdfResponse> generatePdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody GeneratePdfRequest request) {

        return ApiResponse.success(
                fileService.generatePdf(principal.getId(), request));
    }

    /**
     * POST /files/upload
     *
     * Validates the multipart file, uploads it to S3, persists metadata,
     * and returns a pre-signed download URL.
     *
     * Form params:
     *   - file     (required) — the file binary
     *   - resumeId (optional) — links the upload to an existing resume
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FileUploadResponse> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam MultipartFile file,
            @RequestParam(required = false) UUID resumeId) {

        return ApiResponse.success(
                fileService.uploadFile(principal.getId(), resumeId, file));
    }

    /**
     * DELETE /files/{fileId}
     *
     * Deletes the S3 object and the metadata row.
     * Only the owning user can delete their files.
     */
    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID fileId) {

        fileService.deleteFile(fileId, principal.getId());
    }
}
