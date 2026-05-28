package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    // Books
    private Long totalBooks;
    private Long totalBookCopies;
    private Long availableCopies;
    private Long borrowedCopies;

    // Members
    private Long totalMembers;
    private Long activeMembers;

    // Borrowings
    private int activeBorrowings;
    private Long overdueBorrowings;
    private Long borrowingsToday;
    private Long returnsToday;

    // Fines
    private BigDecimal totalUnpaidFines;
    private BigDecimal totalCollectedFines;

    // Reservations
    private Long pendingReservations;
}