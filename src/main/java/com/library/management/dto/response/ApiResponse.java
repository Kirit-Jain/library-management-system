package com.library.management.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private Map<String, String> validationErrors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Static local Methords

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(status.name())
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> validationError(Map<String, String> errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .error("VALIDATION_ERROR")
            .message("Validation failed")
            .validationErrors(errors)
            .build();
    }
}
