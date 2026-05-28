package com.library.management.controller;

import com.library.management.dto.request.MemberCreateRequest;
import com.library.management.dto.request.MemberUpdateRequest;
import com.library.management.dto.response.*;
import com.library.management.service.BorrowingService;
import com.library.management.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member management")
public class MemberController {

    private final MemberService memberService;
    private final BorrowingService borrowingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Get all members")
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(
            ApiResponse.success("Members fetched successfully",memberService.getAllMembers(pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(
        @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Members fetched successfully", memberService.getMemberById(id)));
    }

    @GetMapping("/number/{membershipNumber}")
    @Operation(summary = "Get member by membership number")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberByNumber(
        @PathVariable String membershipNumber) {
        return ResponseEntity.ok(
            ApiResponse.success("Members fetched successfully",
                memberService.getMemberByMembershipNumber(membershipNumber)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Create a new member")
    public ResponseEntity<ApiResponse<MemberResponse>> createMember(
        @Valid @RequestBody MemberCreateRequest request) {

        MemberResponse member = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Member created successfully", member));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    @Operation(summary = "Update member")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
        @PathVariable Long id,
        @Valid @RequestBody MemberUpdateRequest request) {

        return ResponseEntity.ok(
            ApiResponse.success("Member updated", memberService.updateMember(id, request)));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get member full borrowing history and stats")
    public ResponseEntity<ApiResponse<MemberBorrowingHistoryResponse>> getMemberHistory(
        @PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("Member history fetched successfully", borrowingService.getMemberHistory(id)));
    }

    @GetMapping("/my-profile")
    public ResponseEntity<ApiResponse<MemberResponse>> getMyProfile(Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(ApiResponse.success("My profile fetched successfully", memberService.getMyProfile(email)));
    }
}