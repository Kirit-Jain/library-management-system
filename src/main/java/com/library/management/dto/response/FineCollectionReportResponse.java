package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineCollectionReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalCollected;
    private Integer totalPayments;
    private List<DailyCollection> dailyCollections;

    @Data
    @Builder
    public static class DailyCollection {
        private LocalDate date;
        private BigDecimal amount;
        private Integer paymentCount;
    }
}