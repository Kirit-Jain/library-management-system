package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.BookLocationResponse;
import com.library.management.service.BookLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Location", description = "Find where books are physically located")
public class BookLocationController {

    private final BookLocationService bookLocationService;

    @GetMapping("/{id}/location")
    @Operation(summary = "Get exact location of a book (branch, floor, section, shelf)")
    public ResponseEntity<ApiResponse<BookLocationResponse>> getBookLocation(
        @PathVariable Long id) {

        return ResponseEntity.ok(
            ApiResponse.success("Book location fetched successfully", bookLocationService.getBookLocation(id)));
    }
}