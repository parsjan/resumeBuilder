package com.app.resume.controller;

import com.app.common.ApiResponse;
import com.app.resume.dto.ResumeRequest;
import com.app.resume.dto.ResumeResponse;
import com.app.resume.service.ResumeService;
import com.app.user.model.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    // POST /resumes/upload
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ResumeResponse> upload(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(resumeService.uploadResume(principal.getId(), file));
    }

    // POST /resumes
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ResumeResponse> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResumeRequest request) {
        return ApiResponse.success(resumeService.create(principal.getId(), request));
    }

    // GET /resumes
    @GetMapping
    public ApiResponse<Page<ResumeResponse>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ApiResponse.success(resumeService.listByUser(principal.getId(), pageable));
    }

    // GET /resumes/{id}
    @GetMapping("/{id}")
    public ApiResponse<ResumeResponse> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ApiResponse.success(resumeService.getById(principal.getId(), id));
    }

    // PUT /resumes/{id}
    @PutMapping("/{id}")
    public ApiResponse<ResumeResponse> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ResumeRequest request) {
        return ApiResponse.success(resumeService.update(principal.getId(), id, request));
    }

    // PATCH /resumes/{id}
    @PatchMapping("/{id}")
    public ApiResponse<ResumeResponse> patch(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ResumeRequest request) {
        return ApiResponse.success(resumeService.update(principal.getId(), id, request));
    }

    // GET /resumes/{id}/export/pdf
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        byte[] pdf = resumeService.exportPdf(principal.getId(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                .body(pdf);
    }

    // DELETE /resumes/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        resumeService.delete(principal.getId(), id);
    }
}
