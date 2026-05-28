package com.library.management.controller;

import com.library.management.dto.request.BookCopyCreateRequest;
import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.BookCopy;
import com.library.management.service.BookCopyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book-copies")
@RequiredArgsConstructor
@Tag(name = "Book Copies", description = "Physical book copies management")
public class BookCopyController {

    private final BookCopyService bookCopyService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Add a new physical copy of a book")
    public ResponseEntity<ApiResponse<BookCopy>> createCopy(
        @Valid @RequestBody BookCopyCreateRequest request) {

        BookCopy copy = bookCopyService.createCopy(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Book copy added", copy));
    }

    @GetMapping("/book/{bookId}")
    @Operation(summary = "Get all copies of a specific book")
    public ResponseEntity<ApiResponse<List<BookCopy>>> getCopiesByBook(
        @PathVariable Long bookId) {
        return ResponseEntity.ok(
            ApiResponse.success("Book copies found", bookCopyService.getCopiesByBook(bookId)));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Find a copy by barcode")
    public ResponseEntity<ApiResponse<BookCopy>> getByBarcode(
        @PathVariable String barcode) {
        return ResponseEntity.ok(
            ApiResponse.success("Book copy found", bookCopyService.getByBarcode(barcode)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove a book copy")
    public ResponseEntity<ApiResponse<?>> deleteCopy(@PathVariable Long id) {
        bookCopyService.deleteCopy(id);
        return ResponseEntity.ok(ApiResponse.success("Copy removed"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all book copies")
    public ResponseEntity<ApiResponse<List<BookCopy>>> getAll() {
        return ResponseEntity.ok(
            ApiResponse.success("Book copies retrieved successfully", bookCopyService.getAllCopies()));
    }
}