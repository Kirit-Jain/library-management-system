package com.library.management.controller;

import com.library.management.dto.request.UpdateSettingRequest;
import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.LibrarySetting;
import com.library.management.entity.User;
import com.library.management.service.AdminService;
import com.library.management.service.LibrarySettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final LibrarySettingService settingService;

    // ============================================
    //          USER MANAGEMENT
    // ============================================
    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", adminService.getAllUsers(pageable)));
    }

    @PutMapping("/users/{id}/lock")
    @Operation(summary = "Lock a user account")
    public ResponseEntity<ApiResponse<User>> lockUser(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("User locked", adminService.lockUser(id)));
    }

    @PutMapping("/users/{id}/unlock")
    @Operation(summary = "Unlock a user account")
    public ResponseEntity<ApiResponse<User>> unlockUser(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("User unlocked", adminService.unlockUser(id)));
    }

    @PutMapping("/users/{id}/deactivate")
    @Operation(summary = "Deactivate a user account")
    public ResponseEntity<ApiResponse<User>> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("User deactivated", adminService.deactivateUser(id)));
    }

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activate a user account")
    public ResponseEntity<ApiResponse<User>> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success("User activated", adminService.activateUser(id)));
    }

    // ============================================
    //          SETTINGS MANAGEMENT
    // ============================================
    @GetMapping("/settings")
    @Operation(summary = "Get all library settings")
    public ResponseEntity<ApiResponse<List<LibrarySetting>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success("Library settings retrieved", settingService.getAllSettings()));
    }

    @PutMapping("/settings/{key}")
    @Operation(summary = "Update a setting value")
    public ResponseEntity<ApiResponse<LibrarySetting>> updateSetting(
        @PathVariable String key,
        @Valid @RequestBody UpdateSettingRequest request) {

        Long userId = 1L; // TODO: Get from security context
        return ResponseEntity.ok(ApiResponse.success(
            "Setting updated",
            settingService.updateSetting(key, request.getValue(), userId)));
    }
}