package com.app.resume.mapper;

import com.app.resume.dto.ResumeResponse;
import com.app.resume.model.Resume;
import com.app.resume.model.ResumeVersion;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class ResumeMapper {

    /**
     * Maps a {@link Resume} and its latest {@link ResumeVersion} to a {@link ResumeResponse}.
     *
     * @param resume  the resume entity (never null)
     * @param version the latest version snapshot, or {@code null} if none exists yet
     */
    public ResumeResponse toResponse(Resume resume, @Nullable ResumeVersion version) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .title(resume.getTitle())
                .template(resume.getTemplate())
                .versionNo(version != null ? version.getVersionNo() : null)
                .sections(version != null ? version.getContent() : null)
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}
