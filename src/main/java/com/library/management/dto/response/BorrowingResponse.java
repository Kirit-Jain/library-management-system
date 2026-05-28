package com.library.management.dto.response;

import java.time.LocalDateTime;

import com.library.management.enums.BorrowingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private String membershipNumber;
    private Long bookCopyId;
    private String barcode;
    private String bookTitle;
    private String bookIsbn;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private Integer renewedCount;
    private BorrowingStatus status;
    private Boolean isOverdue;
    private Long overdueDays;
    private String notes;
    private LocalDateTime createdAt;
}
