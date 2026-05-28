package com.library.management.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.library.management.dto.request.BookCreateRequest;
import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.BookResponse;
import com.library.management.service.BookService;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book management endpoints")
@SecurityRequirement(name = "Bearer Authentication")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get all books with pagination")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> getAllBooks(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "title") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<BookResponse> books = bookService.getAllBooks(pageable);
        return ResponseEntity.ok(ApiResponse.success("Books fetched successfully", books));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Books fetched successfully",bookService.getBookById(id)));
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN")
    public ResponseEntity<ApiResponse<BookResponse>> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(ApiResponse.success("Books fetched successfully",bookService.getBookByIsbn(isbn)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search books by title, author, or ISBN")
    public ResponseEntity<ApiResponse<Page<BookResponse>>> searchBooks(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            ApiResponse.success("Books fetched successfully",bookService.searchBooks(q, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Create a new book")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(
        @Valid @RequestBody BookCreateRequest request) {

        BookResponse book = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Book created successfully", book));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a book")
    public ResponseEntity<ApiResponse<?>> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));
    }
}