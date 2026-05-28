package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverdueReportResponse {
    private Integer totalOverdueBorrowings;
    private BigDecimal totalOverdueFines;
    private List<OverdueItem> overdueList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverdueItem {
        private Long borrowingId;
        private String memberName;
        private String membershipNumber;
        private String memberEmail;
        private String bookTitle;
        private String barcode;
        private LocalDateTime borrowDate;
        private LocalDateTime dueDate;
        private Long overdueDays;
        private BigDecimal estimatedFine;
    }
}