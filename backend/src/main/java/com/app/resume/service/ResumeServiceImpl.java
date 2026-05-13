package com.app.resume.service;

import com.app.ai.service.AiService;
import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.file.pdf.PdfGenerator;
import com.app.resume.dto.ResumeRequest;
import com.app.resume.dto.ResumeResponse;
import com.app.resume.mapper.ResumeMapper;
import com.app.resume.model.Resume;
import com.app.resume.model.ResumeVersion;
import com.app.resume.repository.ResumeRepository;
import com.app.resume.repository.ResumeVersionRepository;
import com.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository versionRepository;
    private final UserRepository userRepository;
    private final ResumeMapper mapper;
    private final PdfGenerator pdfGenerator;
    private final AiService aiService;
    private final ResumeFileParser fileParser;

    // ── Create ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ResumeResponse create(UUID userId, ResumeRequest request) {
        // getReferenceById returns a proxy — no extra SELECT, just sets the FK
        var user = userRepository.getReferenceById(userId);

        Resume resume = Resume.builder()
                .user(user)
                .title(request.getTitle())
                .template(request.getTemplate())
                .build();

        resume = resumeRepository.save(resume);

        Map<String, Object> content = request.getSections() != null ? request.getSections() : Map.of();
        ResumeVersion version = saveNewVersion(resume, content, 1);

        return mapper.toResponse(resume, version);
    }

    // ── Upload ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ResumeResponse uploadResume(UUID userId, MultipartFile file) {
        log.info("Resume upload: userId={}, filename={}", userId, file.getOriginalFilename());

        String text = fileParser.extractText(file);
        Map<String, Object> sections = aiService.generateResumeFromPrompt(userId, text);

        String title = deriveTitle(file, sections);
        var user = userRepository.getReferenceById(userId);

        Resume resume = Resume.builder()
                .user(user)
                .title(title)
                .build();
        resume = resumeRepository.save(resume);

        ResumeVersion version = saveNewVersion(resume, sections, 1);
        return mapper.toResponse(resume, version);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getById(UUID userId, UUID resumeId) {
        Resume resume = findActiveOrThrow(userId, resumeId);

        ResumeVersion latest = versionRepository
                .findFirstByResumeIdOrderByVersionNoDesc(resumeId)
                .orElse(null);

        return mapper.toResponse(resume, latest);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ResumeResponse> listByUser(UUID userId, Pageable pageable) {
        Page<Resume> page = resumeRepository.findByUserIdAndIsDeletedFalse(userId, pageable);

        if (page.isEmpty()) {
            return page.map(r -> mapper.toResponse(r, null));
        }

        // Single query for all latest versions — avoids N+1
        List<UUID> resumeIds = page.map(Resume::getId).toList();
        Map<UUID, ResumeVersion> latestByResumeId = versionRepository
                .findLatestForResumes(resumeIds)
                .stream()
                .collect(Collectors.toMap(v -> v.getResume().getId(), Function.identity()));

        return page.map(resume ->
                mapper.toResponse(resume, latestByResumeId.get(resume.getId())));
    }

    // ── Update ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ResumeResponse update(UUID userId, UUID resumeId, ResumeRequest request) {
        Resume resume = findActiveOrThrow(userId, resumeId);

        resume.setTitle(request.getTitle());
        if (request.getTemplate() != null) {
            resume.setTemplate(request.getTemplate());
        }
        resumeRepository.save(resume);

        int nextVersionNo = versionRepository.findMaxVersionNoByResumeId(resumeId) + 1;
        Map<String, Object> content = request.getSections() != null ? request.getSections() : Map.of();
        ResumeVersion newVersion = saveNewVersion(resume, content, nextVersionNo);

        return mapper.toResponse(resume, newVersion);
    }

    // ── Delete (soft) ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(UUID userId, UUID resumeId) {
        int affected = resumeRepository.softDelete(resumeId, userId);
        if (affected == 0) {
            throw new AppException(ErrorCode.RESUME_NOT_FOUND);
        }
    }

    // ── Export ────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public byte[] exportPdf(UUID userId, UUID resumeId) {
        Resume resume = findActiveOrThrow(userId, resumeId);
        ResumeVersion version = versionRepository
                .findFirstByResumeIdOrderByVersionNoDesc(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND,
                        "Resume has no content to export"));
        return pdfGenerator.generate(resume, version);
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private Resume findActiveOrThrow(UUID userId, UUID resumeId) {
        return resumeRepository
                .findByIdAndUserIdAndIsDeletedFalse(resumeId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
    }

    private String deriveTitle(MultipartFile file, Map<String, Object> sections) {
        Object pi = sections.get("personalInfo");
        if (pi instanceof Map<?, ?> map && map.get("fullName") instanceof String name && !name.isBlank()) {
            return name + "'s Resume";
        }
        String filename = file.getOriginalFilename();
        if (filename != null) {
            int dot = filename.lastIndexOf('.');
            return dot > 0 ? filename.substring(0, dot) : filename;
        }
        return "Uploaded Resume";
    }

    private ResumeVersion saveNewVersion(Resume resume, Map<String, Object> content, int versionNo) {
        ResumeVersion version = ResumeVersion.builder()
                .resume(resume)
                .content(content)
                .versionNo(versionNo)
                .build();
        return versionRepository.save(version);
    }
}
