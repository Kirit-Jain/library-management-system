package com.library.management.dto.response;

import com.library.management.enums.BorrowRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequestResponse {
    private Long id;

    // Member details (flattened)
    private Long memberId;
    private String memberName;
    private String membershipNumber;
    private String memberEmail;

    // Book details (flattened)
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;

    // Request details
    private BorrowRequestStatus status;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
}