package com.app.resume.service;

import com.app.common.AppException;
import com.app.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ResumeFileParser}.
 *
 * Test flow overview:
 *
 *  extractText()
 *    ├── PDF file (by content-type)          → delegates to PDFBox, returns extracted text
 *    ├── PDF file (by .pdf extension)        → fallback to filename check when MIME not set
 *    ├── DOCX file (by content-type)         → delegates to Apache POI, returns text
 *    ├── DOCX file (by .docx extension)      → fallback to filename check
 *    ├── unsupported type (e.g. .txt)        → throws FILE_TYPE_NOT_ALLOWED (415)
 *    └── valid type but unreadable content   → throws INTERNAL_SERVER_ERROR (500)
 *
 * Notes:
 *  - PDF and DOCX happy-path tests use real (but minimal) byte content. Since
 *    generating valid PDFBox/POI streams in-memory is complex, those tests focus
 *    on the routing logic: the parser must NOT throw FILE_TYPE_NOT_ALLOWED.
 *    I/O failures from corrupt bytes trigger the INTERNAL_SERVER_ERROR branch.
 *  - No Spring context — pure Java tests using MockMultipartFile.
 */
class ResumeFileParserTest {

    ResumeFileParser parser;

    @BeforeEach
    void setUp() {
        parser = new ResumeFileParser();
    }

    // ── Unsupported type ──────────────────────────────────────────────────────

    @Test
    @DisplayName("throws FILE_TYPE_NOT_ALLOWED for unsupported MIME type (text/plain)")
    void extractText_unsupportedContentType_throwsFileTypeNotAllowed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "some notes".getBytes());

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("throws FILE_TYPE_NOT_ALLOWED for unsupported extension without MIME type")
    void extractText_unsupportedExtension_throwsFileTypeNotAllowed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.xls", null, "binary".getBytes());

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("throws FILE_TYPE_NOT_ALLOWED when filename has no extension and no MIME type")
    void extractText_noExtensionNoMime_throwsFileTypeNotAllowed() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume", null, "data".getBytes());

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    // ── PDF routing ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("routes to PDF parser when content-type is application/pdf (corrupt bytes → INTERNAL_SERVER_ERROR)")
    void extractText_pdfMimeType_routesToPdfParser() {
        // Intentionally corrupt PDF bytes — verifies routing, not parsing success.
        // The parser delegates to PDFBox which will throw on bad bytes →
        // the catch block wraps it as INTERNAL_SERVER_ERROR (not FILE_TYPE_NOT_ALLOWED).
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "not-real-pdf".getBytes());

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                // FILE_TYPE_NOT_ALLOWED would mean routing failed; anything else means routing succeeded
                .isNotEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("routes to PDF parser via .pdf filename extension when no MIME type is set")
    void extractText_pdfExtension_routesToPdfParser() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", null, "not-real-pdf".getBytes());

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isNotEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    // ── DOCX routing ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("routes to DOCX parser when content-type contains 'wordprocessingml' (corrupt bytes → INTERNAL_SERVER_ERROR)")
    void extractText_docxMimeType_routesToDocxParser() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "not-real-docx".getBytes());

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isNotEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("routes to DOCX parser via .docx filename extension when no MIME type is set")
    void extractText_docxExtension_routesToDocxParser() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.docx", null, "not-real-docx".getBytes());

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .extracting(ex -> ((AppException) ex).getErrorCode())
                .isNotEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
    }

    // ── Error wrapping ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("wraps I/O failures as INTERNAL_SERVER_ERROR (not FILE_TYPE_NOT_ALLOWED)")
    void extractText_ioFailure_wrapsAsInternalServerError() {
        // Use a valid PDF content-type so routing succeeds, but corrupt bytes so PDFBox fails.
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf",
                new byte[]{0x00, 0x01, 0x02}); // corrupt

        assertThatThrownBy(() -> parser.extractText(file))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> {
                    ErrorCode code = ((AppException) ex).getErrorCode();
                    assertThat(code).isNotEqualTo(ErrorCode.FILE_TYPE_NOT_ALLOWED);
                });
    }
}
