package com.library.management.controller;

import com.library.management.dto.request.BorrowRequest;
import com.library.management.dto.request.ReturnRequest;
import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.BorrowingResponse;
import com.library.management.enums.BorrowingStatus;
import com.library.management.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
@Tag(name = "Borrowings", description = "Book borrowing and returning")
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all borrowings")
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getAllBorrowings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) BorrowingStatus status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("borrowDate").descending());

        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success(
                "Borrowings fetched successfully", borrowingService.getBorrowingsByStatus(status, pageable)));
        }

        return ResponseEntity.ok(
            ApiResponse.success("Borrowings fetched successfully", borrowingService.getAllBorrowings(pageable)));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all overdue borrowings")
    public ResponseEntity<ApiResponse<List<BorrowingResponse>>> getOverdue() {
        return ResponseEntity.ok(
            ApiResponse.success("Overdue borrowings fetched successfully", borrowingService.getOverdueBorrowings()));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's borrowings")
    public ResponseEntity<ApiResponse<List<BorrowingResponse>>> getMyBorrowings(
        Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(ApiResponse.success("My borrowings fetched successfully",
            borrowingService.getMyBorrowings(email)));
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Borrow a book")
    public ResponseEntity<ApiResponse<BorrowingResponse>> borrowBook(
        @Valid @RequestBody BorrowRequest request) {

        // TODO: Get actual logged in user ID from security context
        Long issuedBy = 1L;
        BorrowingResponse response = borrowingService.borrowBook(request, issuedBy);
        return ResponseEntity.ok(
            ApiResponse.success("Book borrowed successfully", response));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Return a borrowed book")
    public ResponseEntity<ApiResponse<BorrowingResponse>> returnBook(
        @PathVariable Long id,
        @Valid @RequestBody ReturnRequest request) {

        request.setBorrowingId(id);
        Long returnedBy = 1L;
        BorrowingResponse response = borrowingService.returnBook(request, returnedBy);
        return ResponseEntity.ok(
            ApiResponse.success("Book returned successfully", response));
    }

    @PostMapping("/{id}/renew")
    @Operation(summary = "Renew a borrowing")
    public ResponseEntity<ApiResponse<BorrowingResponse>> renewBook(
        @PathVariable Long id) {

        BorrowingResponse response = borrowingService.renewBook(id);
        return ResponseEntity.ok(
            ApiResponse.success("Book renewed successfully", response));
    }

    
}