package com.app.resume.mapper;

import com.app.resume.dto.ResumeResponse;
import com.app.resume.model.Resume;
import com.app.resume.model.ResumeVersion;
import com.app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ResumeMapper}.
 *
 * Flow:
 *  1. Build a bare Resume entity (id + timestamps set manually via BaseEntity setters).
 *  2. Optionally pair it with a ResumeVersion snapshot.
 *  3. Call mapper.toResponse() and assert every field is mapped correctly.
 *
 * No Spring context — pure Java object tests.
 */
class ResumeMapperTest {

    ResumeMapper mapper;

    UUID resumeId;
    Instant now;
    Resume resume;

    @BeforeEach
    void setUp() {
        mapper = new ResumeMapper();

        resumeId = UUID.randomUUID();
        now = Instant.now();

        User user = new User();
        user.setId(UUID.randomUUID());

        resume = Resume.builder()
                .user(user)
                .title("Software Engineer Resume")
                .template("modern")
                .build();
        resume.setId(resumeId);
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);
    }

    // ── Identity fields ──────────────────────────────────────────────────────

    @Test
    @DisplayName("maps id, title, template, and timestamps from Resume entity")
    void toResponse_mapsAllResumeFields() {
        ResumeVersion version = buildVersion(Map.of("summary", "Engineer"), 1);

        ResumeResponse result = mapper.toResponse(resume, version);

        assertThat(result.getId()).isEqualTo(resumeId);
        assertThat(result.getTitle()).isEqualTo("Software Engineer Resume");
        assertThat(result.getTemplate()).isEqualTo("modern");
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);
    }

    // ── Version fields ───────────────────────────────────────────────────────

    @Test
    @DisplayName("maps versionNo and sections from the provided ResumeVersion")
    void toResponse_mapsVersionFieldsWhenVersionPresent() {
        Map<String, Object> content = Map.of("skills", List.of("Java", "Spring Boot"));
        ResumeVersion version = buildVersion(content, 3);

        ResumeResponse result = mapper.toResponse(resume, version);

        assertThat(result.getVersionNo()).isEqualTo(3);
        assertThat(result.getSections()).isEqualTo(content);
    }

    @Test
    @DisplayName("sets versionNo and sections to null when version argument is null")
    void toResponse_nullVersion_setsVersionFieldsToNull() {
        ResumeResponse result = mapper.toResponse(resume, null);

        assertThat(result.getVersionNo()).isNull();
        assertThat(result.getSections()).isNull();
    }

    @Test
    @DisplayName("still returns correct resume identity fields when version is null")
    void toResponse_nullVersion_resumeIdentityFieldsStillMapped() {
        ResumeResponse result = mapper.toResponse(resume, null);

        assertThat(result.getId()).isEqualTo(resumeId);
        assertThat(result.getTitle()).isEqualTo("Software Engineer Resume");
        assertThat(result.getTemplate()).isEqualTo("modern");
    }

    @Test
    @DisplayName("maps versionNo=1 for an initial draft version")
    void toResponse_version1_mapsVersionNoAsOne() {
        ResumeVersion version = buildVersion(Map.of(), 1);

        ResumeResponse result = mapper.toResponse(resume, version);

        assertThat(result.getVersionNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("sections in response reflect the exact content map from the version")
    void toResponse_sectionsMirrorVersionContent() {
        Map<String, Object> content = Map.of(
                "personalInfo", Map.of("fullName", "Jane Doe", "email", "jane@example.com"),
                "experience",   List.of(Map.of("company", "Acme", "role", "Engineer")));
        ResumeVersion version = buildVersion(content, 2);

        ResumeResponse result = mapper.toResponse(resume, version);

        assertThat(result.getSections()).containsEntry("personalInfo",
                Map.of("fullName", "Jane Doe", "email", "jane@example.com"));
        assertThat(result.getSections()).containsKey("experience");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private ResumeVersion buildVersion(Map<String, Object> content, int versionNo) {
        ResumeVersion v = ResumeVersion.builder()
                .resume(resume)
                .content(content)
                .versionNo(versionNo)
                .build();
        v.setId(UUID.randomUUID());
        return v;
    }
}
