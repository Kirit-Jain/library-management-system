package com.library.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {
    
    @NotNull(message = "Member ID is required")
    private Long MemberId;

    @NotBlank(message = "Book barcode is required")
    private String barcode;

    private String notes;
}
