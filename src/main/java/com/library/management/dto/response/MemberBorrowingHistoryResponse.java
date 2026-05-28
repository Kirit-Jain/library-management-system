package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberBorrowingHistoryResponse {

    private Long memberId;
    private String memberName;
    private String membershipNumber;

    // Stats
    private Integer totalBorrowed;
    private Integer currentlyBorrowed;
    private Integer totalOverdue;
    private Integer totalReturned;
    private BigDecimal totalFinesPaid;
    private BigDecimal totalFinesPending;

    // Lists
    private List<BorrowingResponse> activeBorrowings;
    private List<BorrowingResponse> borrowingHistory;
    private List<FineResponse> pendingFines;
}