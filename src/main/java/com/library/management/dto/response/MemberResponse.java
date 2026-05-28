package com.library.management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.library.management.enums.MembershipType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {

    private Long id;
    private Long userId;
    private String membershipNumber;
    private String fullName;
    private String email;
    private String phone;
    private MembershipType membershipType;
    private Integer maxBooksAllowed;
    private Integer maxBorrowDays;
    private LocalDate membershipStartDate;
    private LocalDate membershipExpiryDate;
    private Boolean isActive;
    private Boolean membershipExpired;
    private BigDecimal totalFinesPending;
    private Integer currentBorrowedBooksCount;
    private LocalDateTime createdAt;
}
