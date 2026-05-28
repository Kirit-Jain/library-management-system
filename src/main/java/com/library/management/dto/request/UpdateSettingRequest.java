package com.library.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateSettingRequest {

    @NotBlank(message = "Setting value is required")
    private String value;

    private String description;
}