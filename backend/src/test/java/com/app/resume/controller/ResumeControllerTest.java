package com.app.resume.controller;

import com.app.auth.oauth2.CustomOAuth2UserService;
import com.app.auth.oauth2.OAuth2AuthenticationFailureHandler;
import com.app.auth.oauth2.OAuth2AuthenticationSuccessHandler;
import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.config.security.JwtAuthenticationFilter;
import com.app.resume.dto.ResumeRequest;
import com.app.resume.dto.ResumeResponse;
import com.app.resume.service.ResumeService;
import com.app.user.model.User;
import com.app.user.model.UserPrincipal;
import com.app.user.service.EmailUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests for {@link ResumeController} using MockMvc.
 *
 * Test flow overview:
 *
 *  POST /resumes
 *    ├── valid request with title → 201 + ApiResponse with resume data
 *    ├── blank title → 400 validation error
 *    ├── title exceeding 200 chars → 400 validation error
 *    └── unauthenticated → 401
 *
 *  POST /resumes/upload
 *    ├── valid multipart file → 201 + parsed ResumeResponse
 *    └── unauthenticated → 401
 *
 *  GET /resumes
 *    ├── authenticated → 200 with paginated response
 *    └── unauthenticated → 401
 *
 *  GET /resumes/{id}
 *    ├── found → 200 + resume data
 *    ├── service throws RESUME_NOT_FOUND → 404 with error code
 *    └── unauthenticated → 401
 *
 *  PUT /resumes/{id}
 *    ├── valid request → 200 with updated data
 *    └── blank title → 400 validation error
 *
 *  PATCH /resumes/{id}
 *    └── valid request → 200 (delegates to same update() service method as PUT)
 *
 *  DELETE /resumes/{id}
 *    ├── exists → 204 No Content
 *    ├── not found → 404 with RESUME_NOT_FOUND error code
 *    └── unauthenticated → 401
 *
 *  GET /resumes/{id}/export/pdf
 *    ├── found → 200 application/pdf with Content-Disposition attachment header
 *    ├── not found → 404
 *    └── unauthenticated → 401
 *
 * Security setup:
 *  - Real SecurityConfig is loaded; its required collaborator beans are provided as @MockBean.
 *  - JwtAuthenticationFilter mock is stubbed in @BeforeEach to call chain.doFilter() (pass-through).
 *  - Authenticated requests use SecurityMockMvcRequestPostProcessors.user(UserPrincipal).
 *  - Unauthenticated requests omit .with(user(...)) → SecurityConfig rejects them with 401.
 */
@WebMvcTest(controllers = ResumeController.class)
class ResumeControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // ── Service under test ────────────────────────────────────────────────
    @MockBean ResumeService resumeService;

    // ── Security collaborators required by SecurityConfig ─────────────────
    @MockBean JwtAuthenticationFilter        jwtAuthFilter;
    @MockBean EmailUserDetailsService        emailUserDetailsService;
    @MockBean CustomOAuth2UserService        customOAuth2UserService;
    @MockBean OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;
    @MockBean OAuth2AuthenticationFailureHandler oAuth2FailureHandler;
    @MockBean CorsConfigurationSource        corsConfigurationSource;

    // ── Shared fixtures ───────────────────────────────────────────────────
    UUID          userId;
    UUID          resumeId;
    UserPrincipal principal;
    ResumeResponse sampleResponse;

    @BeforeEach
    void setUp() throws Exception {
        userId   = UUID.randomUUID();
        resumeId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        principal = new UserPrincipal(user);

        sampleResponse = ResumeResponse.builder()
                .id(resumeId)
                .title("My Resume")
                .template("modern")
                .versionNo(1)
                .sections(Map.of("summary", "Software Engineer"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        // Make the JWT filter mock pass all requests through to the security filter chain
        // without modifying the SecurityContext, so that:
        //   - requests with .with(user(principal)) are authenticated
        //   - requests without .with(user(...)) are unauthenticated → 401
        willAnswer(inv -> {
            ((FilterChain) inv.getArgument(2))
                    .doFilter(
                            (HttpServletRequest)  inv.getArgument(0),
                            (HttpServletResponse) inv.getArgument(1));
            return null;
        }).given(jwtAuthFilter)
          .doFilter(any(HttpServletRequest.class),
                    any(HttpServletResponse.class),
                    any(FilterChain.class));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /resumes
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /resumes")
    class CreateResumeTests {

        @Test
        @DisplayName("returns 201 and ApiResponse with resume data on valid request")
        void create_validRequest_returns201() throws Exception {
            ResumeRequest request = buildRequest("My Resume", "modern");
            given(resumeService.create(eq(userId), any(ResumeRequest.class)))
                    .willReturn(sampleResponse);

            mockMvc.perform(post("/resumes")
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(resumeId.toString()))
                    .andExpect(jsonPath("$.data.title").value("My Resume"))
                    .andExpect(jsonPath("$.data.versionNo").value(1));
        }

        @Test
        @DisplayName("returns 400 when title is blank")
        void create_blankTitle_returns400() throws Exception {
            ResumeRequest request = buildRequest("", null);

            mockMvc.perform(post("/resumes")
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("returns 400 when title exceeds 200 characters")
        void create_titleTooLong_returns400() throws Exception {
            ResumeRequest request = buildRequest("A".repeat(201), null);

            mockMvc.perform(post("/resumes")
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("returns 400 when request body is missing entirely")
        void create_missingBody_returns400() throws Exception {
            mockMvc.perform(post("/resumes")
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 401 when request is unauthenticated")
        void create_unauthenticated_returns401() throws Exception {
            mockMvc.perform(post("/resumes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("Resume", null))))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // POST /resumes/upload
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /resumes/upload")
    class UploadResumeTests {

        @Test
        @DisplayName("returns 201 with parsed ResumeResponse on successful file upload")
        void upload_validFile_returns201() throws Exception {
            given(resumeService.uploadResume(eq(userId), any()))
                    .willReturn(sampleResponse);

            mockMvc.perform(multipart("/resumes/upload")
                            .file("file", "resume content".getBytes())
                            .with(user(principal))
                            .contentType(MediaType.MULTIPART_FORM_DATA))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("My Resume"));
        }

        @Test
        @DisplayName("returns 401 when unauthenticated")
        void upload_unauthenticated_returns401() throws Exception {
            mockMvc.perform(multipart("/resumes/upload")
                            .file("file", "content".getBytes()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /resumes
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /resumes")
    class ListResumesTests {

        @Test
        @DisplayName("returns 200 with paginated resume list and total element count")
        void list_authenticated_returns200WithPage() throws Exception {
            Page<ResumeResponse> page = new PageImpl<>(List.of(sampleResponse));
            given(resumeService.listByUser(eq(userId), any(Pageable.class)))
                    .willReturn(page);

            mockMvc.perform(get("/resumes")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].id").value(resumeId.toString()))
                    .andExpect(jsonPath("$.data.content[0].title").value("My Resume"))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("returns 200 with empty content list when user has no resumes")
        void list_noResumes_returnsEmptyPage() throws Exception {
            given(resumeService.listByUser(eq(userId), any(Pageable.class)))
                    .willReturn(Page.empty());

            mockMvc.perform(get("/resumes")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        @DisplayName("returns 401 when unauthenticated")
        void list_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/resumes"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /resumes/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /resumes/{id}")
    class GetByIdTests {

        @Test
        @DisplayName("returns 200 with full resume data including sections and versionNo")
        void getById_found_returns200() throws Exception {
            given(resumeService.getById(userId, resumeId)).willReturn(sampleResponse);

            mockMvc.perform(get("/resumes/{id}", resumeId)
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(resumeId.toString()))
                    .andExpect(jsonPath("$.data.versionNo").value(1))
                    .andExpect(jsonPath("$.data.template").value("modern"));
        }

        @Test
        @DisplayName("returns 404 with RESUME_NOT_FOUND error code when service throws AppException")
        void getById_notFound_returns404() throws Exception {
            given(resumeService.getById(userId, resumeId))
                    .willThrow(new AppException(ErrorCode.RESUME_NOT_FOUND));

            mockMvc.perform(get("/resumes/{id}", resumeId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("RESUME_NOT_FOUND"));
        }

        @Test
        @DisplayName("returns 401 when unauthenticated")
        void getById_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/resumes/{id}", resumeId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PUT /resumes/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /resumes/{id}")
    class UpdateResumeTests {

        @Test
        @DisplayName("returns 200 with updated resume data on valid request")
        void update_validRequest_returns200() throws Exception {
            ResumeResponse updated = ResumeResponse.builder()
                    .id(resumeId).title("Updated Resume").versionNo(2).build();
            given(resumeService.update(eq(userId), eq(resumeId), any()))
                    .willReturn(updated);

            mockMvc.perform(put("/resumes/{id}", resumeId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("Updated Resume", null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("Updated Resume"))
                    .andExpect(jsonPath("$.data.versionNo").value(2));
        }

        @Test
        @DisplayName("returns 400 when title is blank on update")
        void update_blankTitle_returns400() throws Exception {
            mockMvc.perform(put("/resumes/{id}", resumeId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("   ", null))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 404 when resume does not exist for the authenticated user")
        void update_notFound_returns404() throws Exception {
            given(resumeService.update(eq(userId), eq(resumeId), any()))
                    .willThrow(new AppException(ErrorCode.RESUME_NOT_FOUND));

            mockMvc.perform(put("/resumes/{id}", resumeId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("Title", null))))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("RESUME_NOT_FOUND"));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PATCH /resumes/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PATCH /resumes/{id}")
    class PatchResumeTests {

        @Test
        @DisplayName("returns 200 and delegates to the same update() service method as PUT")
        void patch_validRequest_returns200() throws Exception {
            given(resumeService.update(eq(userId), eq(resumeId), any()))
                    .willReturn(sampleResponse);

            mockMvc.perform(patch("/resumes/{id}", resumeId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("Patched Resume", null))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            then(resumeService).should().update(eq(userId), eq(resumeId), any());
        }

        @Test
        @DisplayName("returns 400 when title is blank in PATCH request")
        void patch_blankTitle_returns400() throws Exception {
            mockMvc.perform(patch("/resumes/{id}", resumeId)
                            .with(user(principal))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(buildRequest("", null))))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // DELETE /resumes/{id}
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /resumes/{id}")
    class DeleteResumeTests {

        @Test
        @DisplayName("returns 204 No Content when resume is successfully soft-deleted")
        void delete_success_returns204() throws Exception {
            willDoNothing().given(resumeService).delete(userId, resumeId);

            mockMvc.perform(delete("/resumes/{id}", resumeId)
                            .with(user(principal)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 with RESUME_NOT_FOUND when service throws AppException")
        void delete_notFound_returns404() throws Exception {
            willThrow(new AppException(ErrorCode.RESUME_NOT_FOUND))
                    .given(resumeService).delete(userId, resumeId);

            mockMvc.perform(delete("/resumes/{id}", resumeId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("RESUME_NOT_FOUND"));
        }

        @Test
        @DisplayName("returns 401 when unauthenticated")
        void delete_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete("/resumes/{id}", resumeId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GET /resumes/{id}/export/pdf
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /resumes/{id}/export/pdf")
    class ExportPdfTests {

        @Test
        @DisplayName("returns 200 with application/pdf content-type and attachment header")
        void exportPdf_found_returnsPdfWithCorrectHeaders() throws Exception {
            byte[] pdfBytes = new byte[]{0x25, 0x50, 0x44, 0x46}; // %PDF
            given(resumeService.exportPdf(userId, resumeId)).willReturn(pdfBytes);

            mockMvc.perform(get("/resumes/{id}/export/pdf", resumeId)
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"resume.pdf\""))
                    .andExpect(content().bytes(pdfBytes));
        }

        @Test
        @DisplayName("returns 404 when resume has no content to export")
        void exportPdf_notFound_returns404() throws Exception {
            given(resumeService.exportPdf(userId, resumeId))
                    .willThrow(new AppException(ErrorCode.RESUME_NOT_FOUND,
                            "Resume has no content to export"));

            mockMvc.perform(get("/resumes/{id}/export/pdf", resumeId)
                            .with(user(principal)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("RESUME_NOT_FOUND"));
        }

        @Test
        @DisplayName("returns 401 when unauthenticated")
        void exportPdf_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/resumes/{id}/export/pdf", resumeId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResumeRequest buildRequest(String title, String template) {
        ResumeRequest r = new ResumeRequest();
        r.setTitle(title);
        r.setTemplate(template);
        return r;
    }
}
