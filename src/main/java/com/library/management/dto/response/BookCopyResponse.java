// dto/response/BookCopyResponseDto.java
package com.library.management.dto.response;

import com.library.management.enums.BookCondition;
import com.library.management.enums.BookCopyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCopyResponse {

    private Long id;
    private String barcode;
    private BookCopyStatus status;
    private BookCondition condition;
    private BigDecimal price;
    private LocalDate acquisitionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Flat fields from related entities (no nested objects)
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;

    private Long branchId;
    private String branchName;

    private Long shelfId;
    private String shelfCode;

    private Long publisherId;
    private String publisherName;
}