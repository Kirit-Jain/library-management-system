package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.BorrowRequestResponse;
import com.library.management.service.BorrowRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/borrow-requests")
@RequiredArgsConstructor
@Tag(name = "Borrow Requests", description = "Member borrow requests")
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> createRequest(
        @RequestBody Map<String, Long> body) {

        BorrowRequestResponse request = borrowRequestService.createRequest(
            body.get("memberId"), body.get("bookId"));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Borrow request submitted", request));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<Page<BorrowRequestResponse>>> getPending(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            ApiResponse.success("Pending borrow requests retrieved", borrowRequestService.getPendingRequests(pageable)));
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<BorrowRequestResponse>>> getMemberRequests(
        @PathVariable Long memberId) {
        return ResponseEntity.ok(
            ApiResponse.success("Member borrow requests retrieved", borrowRequestService.getMemberRequests(memberId)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> approve(@PathVariable Long id) {
        Long approverId = 1L; // TODO: Get from auth context
        return ResponseEntity.ok(
            ApiResponse.success("Request approved",
                borrowRequestService.approveRequest(id, approverId)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> reject(
        @PathVariable Long id,
        @RequestParam String reason) {
        Long approverId = 1L;
        return ResponseEntity.ok(
            ApiResponse.success("Request rejected",
                borrowRequestService.rejectRequest(id, approverId, reason)));
    }
}