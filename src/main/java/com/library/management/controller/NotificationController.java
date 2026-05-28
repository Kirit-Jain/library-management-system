package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.NotificationResponse;
import com.library.management.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            ApiResponse.success(
                "User notifications fetched successfully",
                notificationService.getUserNotifications(userId, pageable)));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Get unread notification count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
        @PathVariable Long userId) {

        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(
            ApiResponse.success("Unread notification count fetched successfully", Map.of("unreadCount", count)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<ApiResponse<?>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all user notifications as read")
    public ResponseEntity<ApiResponse<?>> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }
}