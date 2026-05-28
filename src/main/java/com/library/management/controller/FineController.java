package com.library.management.controller;

import com.library.management.dto.request.FinePaymentRequest;
import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.FineResponse;
import com.library.management.service.FineService;
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
@RequestMapping("/api/v1/fines")
@RequiredArgsConstructor
@Tag(name = "Fines", description = "Fine management and payments")
public class FineController {

    private final FineService fineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all fines")
    public ResponseEntity<ApiResponse<Page<FineResponse>>> getAllFines(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Fines fetched successfully", fineService.getAllFines(pageable)));
    }

    @GetMapping("/unpaid")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all unpaid fines")
    public ResponseEntity<ApiResponse<Page<FineResponse>>> getUnpaidFines(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            ApiResponse.success("Unpaid fines fetched successfully", fineService.getUnpaidFines(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get fine by ID")
    public ResponseEntity<ApiResponse<FineResponse>> getFineById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Fine fetched successfully", fineService.getFineById(id)));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get all fines for a member")
    public ResponseEntity<ApiResponse<List<FineResponse>>> getMemberFines(
        @PathVariable Long memberId) {
        return ResponseEntity.ok(
            ApiResponse.success("Fines fetched successfully", fineService.getMemberFines(memberId)));
    }

    @GetMapping("/my")
    @Operation(summary = "Get current user's fines")
    public ResponseEntity<ApiResponse<List<FineResponse>>> getMyFines(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(ApiResponse.success("My fines fetched successfully", fineService.getMyFines(email)));
    }

    @PostMapping("/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Pay a fine")
    public ResponseEntity<ApiResponse<FineResponse>> payFine(
        @Valid @RequestBody FinePaymentRequest request) {

        Long receivedBy = 1L;
        FineResponse response = fineService.payFine(request, receivedBy);
        return ResponseEntity.ok(
            ApiResponse.success("Fine payment recorded", response));
    }

    @PostMapping("/{id}/waive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Waive a fine (Admin only)")
    public ResponseEntity<ApiResponse<FineResponse>> waiveFine(
        @PathVariable Long id,
        @RequestParam String reason) {

        return ResponseEntity.ok(
            ApiResponse.success("Fine waived", fineService.waiveFine(id, reason)));
    }
}