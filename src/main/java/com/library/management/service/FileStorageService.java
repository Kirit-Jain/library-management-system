package com.library.management.service;

import com.library.management.entity.FileUpload;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileUploadRepository fileUploadRepository;

    @Value("${file.upload.base-path}")
    private String basePath;

    @Value("${file.upload.max-size}")
    private Long maxSize;

    @Value("${file.upload.allowed-image-types}")
    private String allowedImageTypes;

    @Value("${file.upload.book-cover-path}")
    private String bookCoverPath;

    @Value("${file.upload.profile-photo-path}")
    private String profilePhotoPath;

    // ============================================
    //          UPLOAD BOOK COVER
    // ============================================
    @Transactional
    public FileUpload uploadBookCover(MultipartFile file, Long bookId, Long userId)
        throws IOException {
        validateImageFile(file);

        String fileName = generateFileName(file);
        Path targetPath = createPath(bookCoverPath, fileName);

        // Save resized image (max 800x800)
        Thumbnails.of(file.getInputStream())
            .size(800, 800)
            .toFile(targetPath.toFile());

        return saveFileRecord(file, fileName, targetPath.toString(),
            "BOOK", bookId, userId);
    }

    // ============================================
    //          UPLOAD PROFILE PHOTO
    // ============================================
    @Transactional
    public FileUpload uploadProfilePhoto(MultipartFile file, Long userId)
        throws IOException {
        validateImageFile(file);

        String fileName = generateFileName(file);
        Path targetPath = createPath(profilePhotoPath, fileName);

        // Save smaller resized image (max 400x400)
        Thumbnails.of(file.getInputStream())
            .size(400, 400)
            .toFile(targetPath.toFile());

        return saveFileRecord(file, fileName, targetPath.toString(),
            "USER", userId, userId);
    }

    // ============================================
    //          DOWNLOAD FILE
    // ============================================
    public Resource loadFile(String fileName) {
        try {
            FileUpload fileRecord = fileUploadRepository.findByFileName(fileName)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "File", "name", fileName));

            Path filePath = Paths.get(fileRecord.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "name", fileName);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File", "name", fileName);
        }
    }

    // ============================================
    //          DELETE FILE
    // ============================================
    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        FileUpload file = fileUploadRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File", "id", fileId));

        Path filePath = Paths.get(file.getFilePath());
        Files.deleteIfExists(filePath);
        fileUploadRepository.delete(file);

        log.info("File deleted: {}", file.getFileName());
    }

    // ============================================
    //          GET FILES FOR ENTITY
    // ============================================
    public List<FileUpload> getFilesForEntity(String entityType, Long entityId) {
        return fileUploadRepository.findAllByEntityTypeAndEntityId(entityType, entityId);
    }

    // ============================================
    //          PRIVATE HELPERS
    // ============================================

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        if (file.getSize() > maxSize) {
            throw new BadRequestException(
                "File size exceeds maximum allowed: " + (maxSize / 1024 / 1024) + " MB");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || extension.isBlank()) {
            throw new BadRequestException("File must have an extension");
        }

        List<String> allowedTypes = Arrays.asList(
            allowedImageTypes.toLowerCase().split(","));
        if (!allowedTypes.contains(extension.toLowerCase())) {
            throw new BadRequestException(
                "Only " + allowedImageTypes + " files are allowed");
        }
    }

    private String generateFileName(MultipartFile file) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        return UUID.randomUUID().toString() + "." + extension;
    }

    private Path createPath(String subPath, String fileName) throws IOException {
        Path directory = Paths.get(basePath, subPath);
        Files.createDirectories(directory);
        return directory.resolve(fileName);
    }

    private FileUpload saveFileRecord(MultipartFile file, String fileName,
                                       String filePath, String entityType,
                                       Long entityId, Long userId) {
        FileUpload upload = FileUpload.builder()
            .fileName(fileName)
            .originalName(file.getOriginalFilename())
            .filePath(filePath)
            .fileType(file.getContentType())
            .fileSize(file.getSize())
            .entityType(entityType)
            .entityId(entityId)
            .uploadedBy(userId)
            .build();

        return fileUploadRepository.save(upload);
    }
}