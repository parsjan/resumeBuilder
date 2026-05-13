package com.app.resume.service;

import com.app.common.AppException;
import com.app.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Component
public class ResumeFileParser {

    public String extractText(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        try {
            if ("application/pdf".equals(contentType) || filename.endsWith(".pdf")) {
                return extractFromPdf(file);
            } else if ((contentType != null && contentType.contains("wordprocessingml")) || filename.endsWith(".docx")) {
                return extractFromDocx(file);
            } else {
                throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                        "Unsupported file type. Please upload a PDF or DOCX file.");
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to extract text from uploaded file: {}", file.getOriginalFilename(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Could not read the uploaded file.");
        }
    }

    private String extractFromPdf(MultipartFile file) throws IOException {
        try (PDDocument doc = PDDocument.load(file.getInputStream())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String extractFromDocx(MultipartFile file) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
