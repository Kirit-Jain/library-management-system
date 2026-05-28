package com.library.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Book ID is required")
    private Long bookId;
}
