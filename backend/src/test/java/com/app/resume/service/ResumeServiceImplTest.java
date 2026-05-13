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
import com.app.user.model.User;
import com.app.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * Unit tests for {@link ResumeServiceImpl}.
 *
 * Test flow overview:
 *
 *  create()
 *    ├── saves Resume + ResumeVersion(v1) when sections are provided
 *    ├── defaults content to empty map when sections is null
 *    └── passes title and template through to the Resume entity
 *
 *  uploadResume()
 *    ├── file → fileParser.extractText → aiService.generateResumeFromPrompt → Resume saved
 *    ├── title derived from personalInfo.fullName when present
 *    ├── title derived from filename (without extension) when no fullName
 *    └── title falls back to "Uploaded Resume" when filename is null
 *
 *  getById()
 *    ├── returns ResumeResponse with latest version on success
 *    ├── throws RESUME_NOT_FOUND when resume is missing or deleted
 *    └── passes null version to mapper when resume has no versions yet
 *
 *  listByUser()
 *    ├── returns empty page immediately and skips version query
 *    ├── fetches latest versions in ONE batch query to prevent N+1
 *    └── maps null version when no version exists for a resume
 *
 *  update()
 *    ├── increments versionNo (max + 1) and updates title/template
 *    ├── preserves existing template when request template is null
 *    └── throws RESUME_NOT_FOUND when resume missing/deleted
 *
 *  delete()
 *    ├── calls softDelete and completes without exception
 *    └── throws RESUME_NOT_FOUND when softDelete affects 0 rows
 *
 *  exportPdf()
 *    ├── returns PDF bytes from pdfGenerator using resume + latest version
 *    ├── throws RESUME_NOT_FOUND when resume has no version to export
 *    └── throws RESUME_NOT_FOUND when resume does not belong to user
 *
 * All collaborators are mocked — no Spring context, no database.
 */
@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    // ── Mocks ──────────────────────────────────────────────────────────────
    @Mock ResumeRepository        resumeRepository;
    @Mock ResumeVersionRepository versionRepository;
    @Mock UserRepository          userRepository;
    @Mock ResumeMapper            mapper;
    @Mock PdfGenerator            pdfGenerator;
    @Mock AiService               aiService;
    @Mock ResumeFileParser        fileParser;

    @InjectMocks
    ResumeServiceImpl service;

    // ── Shared fixtures ────────────────────────────────────────────────────
    UUID         userId;
    UUID         resumeId;
    User         mockUser;
    Resume       mockResume;
    ResumeVersion mockVersion;
    ResumeResponse mockResponse;

    @BeforeEach
    void setUp() {
        userId   = UUID.randomUUID();
        resumeId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");

        mockResume = Resume.builder()
                .user(mockUser)
                .title("My Resume")
                .template("modern")
                .build();
        mockResume.setId(resumeId);
        mockResume.setCreatedAt(Instant.now());
        mockResume.setUpdatedAt(Instant.now());

        mockVersion = ResumeVersion.builder()
                .resume(mockResume)
                .content(Map.of("summary", "Software Engineer"))
                .versionNo(1)
                .build();
        mockVersion.setId(UUID.randomUUID());

        mockResponse = ResumeResponse.builder()
                .id(resumeId)
                .title("My Resume")
                .template("modern")
                .versionNo(1)
                .sections(Map.of("summary", "Software Engineer"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // create()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("saves Resume entity and ResumeVersion(v1) with the provided sections")
        void create_savesResumeAndVersion1WithSections() {
            ResumeRequest request = buildRequest("My Resume", "modern",
                    Map.of("summary", "Engineer"));

            given(userRepository.getReferenceById(userId)).willReturn(mockUser);
            given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);
            given(versionRepository.save(any(ResumeVersion.class))).willReturn(mockVersion);
            given(mapper.toResponse(mockResume, mockVersion)).willReturn(mockResponse);

            ResumeResponse result = service.create(userId, request);

            assertThat(result).isEqualTo(mockResponse);

            ArgumentCaptor<ResumeVersion> vCap = ArgumentCaptor.forClass(ResumeVersion.class);
            then(versionRepository).should().save(vCap.capture());
            assertThat(vCap.getValue().getVersionNo()).isEqualTo(1);
            assertThat(vCap.getValue().getContent()).containsKey("summary");
        }

        @Test
        @DisplayName("stores empty map as content when request sections is null")
        void create_nullSections_savesEmptyContent() {
            ResumeRequest request = buildRequest("Empty Resume", null, null);

            given(userRepository.getReferenceById(userId)).willReturn(mockUser);
            given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);
            given(versionRepository.save(any(ResumeVersion.class))).willReturn(mockVersion);
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            service.create(userId, request);

            ArgumentCaptor<ResumeVersion> vCap = ArgumentCaptor.forClass(ResumeVersion.class);
            then(versionRepository).should().save(vCap.capture());
            assertThat(vCap.getValue().getContent()).isEmpty();
        }

        @Test
        @DisplayName("copies title and template onto the persisted Resume entity")
        void create_setsResumeFields() {
            ResumeRequest request = buildRequest("Dev Resume", "classic", null);

            given(userRepository.getReferenceById(userId)).willReturn(mockUser);
            given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);
            given(versionRepository.save(any(ResumeVersion.class))).willReturn(mockVersion);
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            service.create(userId, request);

            ArgumentCaptor<Resume> rCap = ArgumentCaptor.forClass(Resume.class);
            then(resumeRepository).should().save(rCap.capture());
            assertThat(rCap.getValue().getTitle()).isEqualTo("Dev Resume");
            assertThat(rCap.getValue().getTemplate()).isEqualTo("classic");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // uploadResume()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("uploadResume()")
    class UploadResumeTests {

        @Test
        @DisplayName("derives title '<fullName>'s Resume' when personalInfo.fullName is present")
        void uploadResume_titleFromFullName() {
            MultipartFile file = mockFile("resume.pdf");

            given(fileParser.extractText(file)).willReturn("raw text");
            given(aiService.generateResumeFromPrompt(userId, "raw text"))
                    .willReturn(Map.of("personalInfo", Map.of("fullName", "John Doe")));
            stubSave();

            service.uploadResume(userId, file);

            ArgumentCaptor<Resume> rCap = ArgumentCaptor.forClass(Resume.class);
            then(resumeRepository).should().save(rCap.capture());
            assertThat(rCap.getValue().getTitle()).isEqualTo("John Doe's Resume");
        }

        @Test
        @DisplayName("derives title from filename (without extension) when no fullName in sections")
        void uploadResume_titleFromFilenameWhenNoFullName() {
            MultipartFile file = mockFile("my_cv.pdf");

            given(fileParser.extractText(file)).willReturn("text");
            given(aiService.generateResumeFromPrompt(userId, "text")).willReturn(Map.of());
            stubSave();

            service.uploadResume(userId, file);

            ArgumentCaptor<Resume> rCap = ArgumentCaptor.forClass(Resume.class);
            then(resumeRepository).should().save(rCap.capture());
            assertThat(rCap.getValue().getTitle()).isEqualTo("my_cv");
        }

        @Test
        @DisplayName("uses 'Uploaded Resume' fallback when filename and fullName are both absent")
        void uploadResume_defaultTitleWhenNoFilenameOrFullName() {
            MultipartFile file = mock(MultipartFile.class);
            given(file.getOriginalFilename()).willReturn(null);
            given(fileParser.extractText(file)).willReturn("text");
            given(aiService.generateResumeFromPrompt(userId, "text")).willReturn(Map.of());
            stubSave();

            service.uploadResume(userId, file);

            ArgumentCaptor<Resume> rCap = ArgumentCaptor.forClass(Resume.class);
            then(resumeRepository).should().save(rCap.capture());
            assertThat(rCap.getValue().getTitle()).isEqualTo("Uploaded Resume");
        }

        @Test
        @DisplayName("saves AI-generated sections as ResumeVersion content")
        void uploadResume_savesAiSectionsAsVersionContent() {
            MultipartFile file = mockFile("resume.pdf");
            Map<String, Object> aiSections = Map.of(
                    "personalInfo", Map.of("fullName", "Jane"),
                    "skills",       List.of("Java"));

            given(fileParser.extractText(file)).willReturn("text");
            given(aiService.generateResumeFromPrompt(userId, "text")).willReturn(aiSections);
            stubSave();

            service.uploadResume(userId, file);

            ArgumentCaptor<ResumeVersion> vCap = ArgumentCaptor.forClass(ResumeVersion.class);
            then(versionRepository).should().save(vCap.capture());
            assertThat(vCap.getValue().getContent()).containsKey("skills");
            assertThat(vCap.getValue().getVersionNo()).isEqualTo(1);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // getById()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("returns response with latest version when resume exists")
        void getById_returnsResumeWithLatestVersion() {
            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(versionRepository.findFirstByResumeIdOrderByVersionNoDesc(resumeId))
                    .willReturn(Optional.of(mockVersion));
            given(mapper.toResponse(mockResume, mockVersion)).willReturn(mockResponse);

            ResumeResponse result = service.getById(userId, resumeId);

            assertThat(result).isEqualTo(mockResponse);
        }

        @Test
        @DisplayName("throws RESUME_NOT_FOUND when resume is absent or soft-deleted")
        void getById_throwsNotFound_whenResumeAbsent() {
            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> service.getById(userId, resumeId))
                    .isInstanceOf(AppException.class)
                    .extracting(ex -> ((AppException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.RESUME_NOT_FOUND);
        }

        @Test
        @DisplayName("passes null version to mapper when resume has no version snapshots yet")
        void getById_nullVersion_whenNoVersionExists() {
            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(versionRepository.findFirstByResumeIdOrderByVersionNoDesc(resumeId))
                    .willReturn(Optional.empty());
            given(mapper.toResponse(mockResume, null)).willReturn(mockResponse);

            service.getById(userId, resumeId);

            then(mapper).should().toResponse(mockResume, null);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // listByUser()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("listByUser()")
    class ListByUserTests {

        @Test
        @DisplayName("returns empty page immediately without querying version repository")
        void listByUser_emptyPage_skipsVersionQuery() {
            Pageable pageable = PageRequest.of(0, 10);
            given(resumeRepository.findByUserIdAndIsDeletedFalse(userId, pageable))
                    .willReturn(Page.empty(pageable));

            Page<ResumeResponse> result = service.listByUser(userId, pageable);

            assertThat(result.isEmpty()).isTrue();
            then(versionRepository).should(never()).findLatestForResumes(any());
        }

        @Test
        @DisplayName("calls findLatestForResumes exactly once with all resume IDs (prevents N+1)")
        void listByUser_batchLoadsLatestVersionsInOneQuery() {
            Pageable pageable = PageRequest.of(0, 10);

            Resume r2 = buildResume("Second Resume");
            ResumeVersion v2 = buildVersion(r2, Map.of(), 1);

            Page<Resume> page = new PageImpl<>(List.of(mockResume, r2), pageable, 2);
            given(resumeRepository.findByUserIdAndIsDeletedFalse(userId, pageable)).willReturn(page);
            given(versionRepository.findLatestForResumes(anyList()))
                    .willReturn(List.of(mockVersion, v2));
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            service.listByUser(userId, pageable);

            then(versionRepository).should(times(1)).findLatestForResumes(anyList());
        }

        @Test
        @DisplayName("maps resume with null version when no version exists for that resume")
        void listByUser_resumeWithNoVersion_mapsWithNull() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Resume> page = new PageImpl<>(List.of(mockResume), pageable, 1);
            given(resumeRepository.findByUserIdAndIsDeletedFalse(userId, pageable)).willReturn(page);
            given(versionRepository.findLatestForResumes(anyList()))
                    .willReturn(Collections.emptyList());
            given(mapper.toResponse(mockResume, null)).willReturn(mockResponse);

            service.listByUser(userId, pageable);

            then(mapper).should().toResponse(mockResume, null);
        }

        @Test
        @DisplayName("returns page with correct total element count")
        void listByUser_returnsCorrectTotalCount() {
            Pageable pageable = PageRequest.of(0, 10);
            Resume r2 = buildResume("Second Resume");
            Page<Resume> page = new PageImpl<>(List.of(mockResume, r2), pageable, 2);
            given(resumeRepository.findByUserIdAndIsDeletedFalse(userId, pageable)).willReturn(page);
            given(versionRepository.findLatestForResumes(anyList())).willReturn(List.of());
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            Page<ResumeResponse> result = service.listByUser(userId, pageable);

            assertThat(result.getTotalElements()).isEqualTo(2);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // update()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("creates new version with versionNo = maxVersionNo + 1")
        void update_incrementsVersionNo() {
            ResumeRequest request = buildRequest("Updated Title", "classic",
                    Map.of("skills", List.of("Java")));

            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(resumeRepository.save(mockResume)).willReturn(mockResume);
            given(versionRepository.findMaxVersionNoByResumeId(resumeId)).willReturn(1);
            given(versionRepository.save(any(ResumeVersion.class))).willReturn(mockVersion);
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            service.update(userId, resumeId, request);

            ArgumentCaptor<ResumeVersion> vCap = ArgumentCaptor.forClass(ResumeVersion.class);
            then(versionRepository).should().save(vCap.capture());
            assertThat(vCap.getValue().getVersionNo()).isEqualTo(2);
        }

        @Test
        @DisplayName("updates title on the Resume entity")
        void update_updatesTitleOnEntity() {
            mockResume.setTitle("Old Title");
            ResumeRequest request = buildRequest("New Title", null, null);

            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(resumeRepository.save(mockResume)).willReturn(mockResume);
            given(versionRepository.findMaxVersionNoByResumeId(resumeId)).willReturn(1);
            given(versionRepository.save(any())).willReturn(mockVersion);
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            service.update(userId, resumeId, request);

            assertThat(mockResume.getTitle()).isEqualTo("New Title");
        }

        @Test
        @DisplayName("does not overwrite template when request template is null")
        void update_preservesTemplate_whenRequestTemplateIsNull() {
            mockResume.setTemplate("modern");
            ResumeRequest request = buildRequest("Title", null, null);

            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(resumeRepository.save(mockResume)).willReturn(mockResume);
            given(versionRepository.findMaxVersionNoByResumeId(resumeId)).willReturn(1);
            given(versionRepository.save(any())).willReturn(mockVersion);
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            service.update(userId, resumeId, request);

            assertThat(mockResume.getTemplate()).isEqualTo("modern");
        }

        @Test
        @DisplayName("throws RESUME_NOT_FOUND when resume does not exist or is deleted")
        void update_throwsNotFound_whenResumeAbsent() {
            ResumeRequest request = buildRequest("Title", null, null);
            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> service.update(userId, resumeId, request))
                    .isInstanceOf(AppException.class)
                    .extracting(ex -> ((AppException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.RESUME_NOT_FOUND);
        }

        @Test
        @DisplayName("uses empty content map when request sections is null on update")
        void update_nullSections_savesEmptyContent() {
            ResumeRequest request = buildRequest("Title", null, null);

            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(resumeRepository.save(mockResume)).willReturn(mockResume);
            given(versionRepository.findMaxVersionNoByResumeId(resumeId)).willReturn(2);
            given(versionRepository.save(any(ResumeVersion.class))).willReturn(mockVersion);
            given(mapper.toResponse(any(), any())).willReturn(mockResponse);

            service.update(userId, resumeId, request);

            ArgumentCaptor<ResumeVersion> vCap = ArgumentCaptor.forClass(ResumeVersion.class);
            then(versionRepository).should().save(vCap.capture());
            assertThat(vCap.getValue().getContent()).isEmpty();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // delete()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("calls softDelete on repository and completes without exception")
        void delete_success() {
            given(resumeRepository.softDelete(resumeId, userId)).willReturn(1);

            assertThatCode(() -> service.delete(userId, resumeId))
                    .doesNotThrowAnyException();
            then(resumeRepository).should().softDelete(resumeId, userId);
        }

        @Test
        @DisplayName("throws RESUME_NOT_FOUND when softDelete affects 0 rows (wrong owner or already deleted)")
        void delete_throwsNotFound_whenNoRowsAffected() {
            given(resumeRepository.softDelete(resumeId, userId)).willReturn(0);

            assertThatThrownBy(() -> service.delete(userId, resumeId))
                    .isInstanceOf(AppException.class)
                    .extracting(ex -> ((AppException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.RESUME_NOT_FOUND);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // exportPdf()
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("exportPdf()")
    class ExportPdfTests {

        @Test
        @DisplayName("returns PDF bytes produced by pdfGenerator for the latest version")
        void exportPdf_returnsPdfBytes() {
            byte[] pdfBytes = {0x25, 0x50, 0x44, 0x46}; // %PDF magic bytes

            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(versionRepository.findFirstByResumeIdOrderByVersionNoDesc(resumeId))
                    .willReturn(Optional.of(mockVersion));
            given(pdfGenerator.generate(mockResume, mockVersion)).willReturn(pdfBytes);

            byte[] result = service.exportPdf(userId, resumeId);

            assertThat(result).isEqualTo(pdfBytes);
        }

        @Test
        @DisplayName("throws RESUME_NOT_FOUND when resume has no version to export")
        void exportPdf_throwsNotFound_whenNoVersion() {
            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(versionRepository.findFirstByResumeIdOrderByVersionNoDesc(resumeId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> service.exportPdf(userId, resumeId))
                    .isInstanceOf(AppException.class)
                    .extracting(ex -> ((AppException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.RESUME_NOT_FOUND);
        }

        @Test
        @DisplayName("throws RESUME_NOT_FOUND when resume does not belong to the requesting user")
        void exportPdf_throwsNotFound_whenResumeNotOwnedByUser() {
            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> service.exportPdf(userId, resumeId))
                    .isInstanceOf(AppException.class)
                    .extracting(ex -> ((AppException) ex).getErrorCode())
                    .isEqualTo(ErrorCode.RESUME_NOT_FOUND);
        }

        @Test
        @DisplayName("delegates to pdfGenerator with correct resume and version arguments")
        void exportPdf_callsPdfGeneratorWithCorrectArgs() {
            byte[] pdfBytes = new byte[]{1, 2, 3};
            given(resumeRepository.findByIdAndUserIdAndIsDeletedFalse(resumeId, userId))
                    .willReturn(Optional.of(mockResume));
            given(versionRepository.findFirstByResumeIdOrderByVersionNoDesc(resumeId))
                    .willReturn(Optional.of(mockVersion));
            given(pdfGenerator.generate(mockResume, mockVersion)).willReturn(pdfBytes);

            service.exportPdf(userId, resumeId);

            then(pdfGenerator).should().generate(mockResume, mockVersion);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ResumeRequest buildRequest(String title, String template,
                                       Map<String, Object> sections) {
        ResumeRequest r = new ResumeRequest();
        r.setTitle(title);
        r.setTemplate(template);
        r.setSections(sections);
        return r;
    }

    private MultipartFile mockFile(String filename) {
        MultipartFile file = mock(MultipartFile.class);
        given(file.getOriginalFilename()).willReturn(filename);
        return file;
    }

    private Resume buildResume(String title) {
        Resume r = Resume.builder().user(mockUser).title(title).build();
        r.setId(UUID.randomUUID());
        r.setCreatedAt(Instant.now());
        r.setUpdatedAt(Instant.now());
        return r;
    }

    private ResumeVersion buildVersion(Resume resume, Map<String, Object> content, int versionNo) {
        ResumeVersion v = ResumeVersion.builder()
                .resume(resume).content(content).versionNo(versionNo).build();
        v.setId(UUID.randomUUID());
        return v;
    }

    // Stubs the save chain used by uploadResume
    private void stubSave() {
        given(userRepository.getReferenceById(userId)).willReturn(mockUser);
        given(resumeRepository.save(any(Resume.class))).willReturn(mockResume);
        given(versionRepository.save(any(ResumeVersion.class))).willReturn(mockVersion);
        given(mapper.toResponse(any(), any())).willReturn(mockResponse);
    }
}
