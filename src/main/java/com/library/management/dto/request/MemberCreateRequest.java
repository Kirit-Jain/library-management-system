package com.library.management.dto.request;

import java.time.LocalDate;

import com.library.management.enums.MembershipType;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberCreateRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Membership type is required")
    private MembershipType membershipType;

    @NotNull(message = "Membership start date is required")
    private LocalDate membershipStartDate;

    @NotNull(message = "Membership expiry date is required")
    @Future(message = "Membership expiry date must be in the future")
    private LocalDate membershipExpiryDate;
}
