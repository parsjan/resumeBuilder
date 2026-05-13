package com.app.file.service;

import com.app.common.AppException;
import com.app.common.ErrorCode;
import com.app.file.config.S3Properties;
import com.app.file.dto.FileUploadResponse;
import com.app.file.dto.GeneratePdfRequest;
import com.app.file.dto.GeneratePdfResponse;
import com.app.file.model.FileMetadata;
import com.app.file.pdf.PdfGenerator;
import com.app.file.repository.FileMetadataRepository;
import com.app.resume.model.Resume;
import com.app.resume.model.ResumeVersion;
import com.app.resume.repository.ResumeRepository;
import com.app.resume.repository.ResumeVersionRepository;
import com.app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    private final FileMetadataRepository fileMetadataRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeVersionRepository versionRepository;
    private final UserRepository userRepository;
    private final PdfGenerator pdfGenerator;

    @Value("${app.file.max-size-mb:10}")
    private int maxSizeMb;

    @Value("${app.file.allowed-types}")
    private String allowedTypesRaw;

    // ── Upload ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public FileUploadResponse uploadFile(UUID userId, UUID resumeId, MultipartFile file) {
        validateFile(file);

        FileMetadata.FileType fileType = detectFileType(file.getContentType());
        String s3Key = buildUploadKey(userId, file.getOriginalFilename());

        putObject(s3Key, readBytes(file), file.getContentType());

        Resume resumeRef = resolveResumeRef(userId, resumeId);

        FileMetadata metadata = FileMetadata.builder()
                .user(userRepository.getReferenceById(userId))
                .resume(resumeRef)
                .fileType(fileType)
                .s3Key(s3Key)
                .originalFileName(file.getOriginalFilename())
                .fileSizeBytes(file.getSize())
                .build();

        metadata = fileMetadataRepository.save(metadata);
        String signedUrl = generateSignedUrl(s3Key);

        log.info("File uploaded: userId={}, s3Key={}, size={}B", userId, s3Key, file.getSize());

        return FileUploadResponse.builder()
                .fileId(metadata.getId())
                .originalFileName(metadata.getOriginalFileName())
                .fileType(metadata.getFileType())
                .fileSizeBytes(metadata.getFileSizeBytes())
                .signedUrl(signedUrl)
                .createdAt(metadata.getCreatedAt())
                .build();
    }

    // ── Generate PDF ──────────────────────────────────────────────────────

    @Override
    @Transactional
    public GeneratePdfResponse generatePdf(UUID userId, GeneratePdfRequest request) {
        UUID resumeId = request.getResumeId();

        Resume resume = resumeRepository
                .findByIdAndUserIdAndIsDeletedFalse(resumeId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));

        ResumeVersion version = versionRepository
                .findFirstByResumeIdOrderByVersionNoDesc(resumeId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND,
                        "Resume has no content to export"));

        byte[] pdfBytes = pdfGenerator.generate(resume, version);
        String fileName = "resume-" + resumeId + "-v" + version.getVersionNo() + ".pdf";
        String s3Key = buildPdfKey(userId, resumeId, version.getVersionNo());

        putObject(s3Key, pdfBytes, "application/pdf");

        FileMetadata metadata = FileMetadata.builder()
                .user(userRepository.getReferenceById(userId))
                .resume(resume)
                .fileType(FileMetadata.FileType.PDF)
                .s3Key(s3Key)
                .originalFileName(fileName)
                .fileSizeBytes((long) pdfBytes.length)
                .build();

        metadata = fileMetadataRepository.save(metadata);
        String signedUrl = generateSignedUrl(s3Key);

        log.info("PDF generated: userId={}, resumeId={}, versionNo={}", userId, resumeId, version.getVersionNo());

        return GeneratePdfResponse.builder()
                .fileId(metadata.getId())
                .resumeId(resumeId)
                .fileName(fileName)
                .signedUrl(signedUrl)
                .createdAt(metadata.getCreatedAt())
                .build();
    }

    // ── Signed URL ────────────────────────────────────────────────────────

    @Override
    public String generateSignedUrl(String s3Key) {
        long expiryMinutes = s3Properties.getS3().getSignedUrlExpirationMinutes();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(s3Properties.getS3().getBucketName())
                        .key(s3Key)
                        .build())
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    // ── Delete ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteFile(UUID fileId, UUID userId) {
        FileMetadata metadata = fileMetadataRepository
                .findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(s3Properties.getS3().getBucketName())
                .key(metadata.getS3Key())
                .build());

        fileMetadataRepository.delete(metadata);

        log.info("File deleted: fileId={}, s3Key={}", fileId, metadata.getS3Key());
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Uploaded file is empty");
        }

        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new AppException(ErrorCode.FILE_TOO_LARGE,
                    "File exceeds the maximum allowed size of " + maxSizeMb + " MB");
        }

        List<String> allowed = Arrays.asList(allowedTypesRaw.split(","));
        String contentType = file.getContentType();
        if (contentType == null || !allowed.contains(contentType.trim())) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                    "File type '" + contentType + "' is not allowed");
        }
    }

    private FileMetadata.FileType detectFileType(String contentType) {
        if (contentType == null) return FileMetadata.FileType.DOC;
        return switch (contentType.trim().toLowerCase()) {
            case "application/pdf"                                                          -> FileMetadata.FileType.PDF;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> FileMetadata.FileType.DOCX;
            case "application/msword"                                                       -> FileMetadata.FileType.DOC;
            case "image/jpeg", "image/png"                                                  -> FileMetadata.FileType.IMAGE;
            default -> FileMetadata.FileType.DOC;
        };
    }

    private void putObject(String s3Key, byte[] bytes, String contentType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(s3Properties.getS3().getBucketName())
                .key(s3Key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();

        try {
            s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));
        } catch (Exception ex) {
            log.error("Failed to upload to S3: key={}, error={}", s3Key, ex.getMessage());
            throw new AppException(ErrorCode.FILE_NOT_FOUND,
                    "Failed to upload file to storage: " + ex.getMessage());
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Could not read uploaded file");
        }
    }

    private Resume resolveResumeRef(UUID userId, UUID resumeId) {
        if (resumeId == null) return null;
        return resumeRepository
                .findByIdAndUserIdAndIsDeletedFalse(resumeId, userId)
                .orElseThrow(() -> new AppException(ErrorCode.RESUME_NOT_FOUND));
    }

    /** S3 key: {@code uploads/{userId}/{epochMs}_{filename}} */
    private String buildUploadKey(UUID userId, String originalFilename) {
        String safeName = originalFilename != null
                ? originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "file";
        return "uploads/%s/%d_%s".formatted(userId, Instant.now().toEpochMilli(), safeName);
    }

    /** S3 key: {@code pdfs/{userId}/resume_{resumeId}_v{versionNo}.pdf} */
    private String buildPdfKey(UUID userId, UUID resumeId, int versionNo) {
        return "pdfs/%s/resume_%s_v%d.pdf".formatted(userId, resumeId, versionNo);
    }
}
