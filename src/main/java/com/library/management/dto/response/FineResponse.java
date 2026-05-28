package com.library.management.dto.response;

import com.library.management.enums.FineStatus;
import com.library.management.enums.FineType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FineResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private String membershipNumber;
    private Long borrowingId;
    private String bookTitle;
    private String barcode;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private FineType fineType;
    private FineStatus status;
    private LocalDate fineDate;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String notes;
    private LocalDateTime createdAt;
}