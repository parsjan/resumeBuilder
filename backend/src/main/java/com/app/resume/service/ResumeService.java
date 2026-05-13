package com.app.resume.service;

import com.app.resume.dto.ResumeRequest;
import com.app.resume.dto.ResumeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ResumeService {

    ResumeResponse create(UUID userId, ResumeRequest request);

    ResumeResponse uploadResume(UUID userId, MultipartFile file);

    ResumeResponse getById(UUID userId, UUID resumeId);

    Page<ResumeResponse> listByUser(UUID userId, Pageable pageable);

    ResumeResponse update(UUID userId, UUID resumeId, ResumeRequest request);

    void delete(UUID userId, UUID resumeId);

    byte[] exportPdf(UUID userId, UUID resumeId);
}
