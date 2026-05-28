package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.FileUpload;
import com.library.management.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "File upload and management")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/book-cover/{bookId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Upload book cover image")
    public ResponseEntity<ApiResponse<FileUpload>> uploadBookCover(
        @PathVariable Long bookId,
        @RequestParam("file") MultipartFile file) throws IOException {

        Long userId = 1L; // TODO: Get from security context
        FileUpload upload = fileStorageService.uploadBookCover(file, bookId, userId);

        return ResponseEntity.ok(
            ApiResponse.success("Book cover uploaded successfully", upload));
    }

    @PostMapping(value = "/profile-photo/{userId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile photo")
    public ResponseEntity<ApiResponse<FileUpload>> uploadProfilePhoto(
        @PathVariable Long userId,
        @RequestParam("file") MultipartFile file) throws IOException {

        FileUpload upload = fileStorageService.uploadProfilePhoto(file, userId);
        return ResponseEntity.ok(
            ApiResponse.success("Profile photo uploaded", upload));
    }

    @GetMapping("/download/{fileName}")
    @Operation(summary = "Download/view a file")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFile(fileName);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + resource.getFilename() + "\"")
            .body(resource);
    }

    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a file")
    public ResponseEntity<ApiResponse<?>> deleteFile(@PathVariable Long fileId)
        throws IOException {
        fileStorageService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted"));
    }
}