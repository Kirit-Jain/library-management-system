package com.library.management.dto.request;

import java.time.LocalDate;
import com.library.management.enums.MembershipType;
import jakarta.validation.constraints.Future;
import lombok.Data;

@Data
public class MemberUpdateRequest {

    private MembershipType membershipType;

    @Future(message = "Membership expiry date must be in the future")
    private LocalDate membershipExpiryDate;

    private Boolean isActive;

    private Integer maxBooksAllowed;

    private Integer maxBorrowDays;
}
