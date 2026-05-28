package com.library.management.dto.request;

import com.library.management.enums.BookCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class BookCopyCreateRequest {

    @NotNull(message = "Book ID is required")
    private Long bookId;

    private String barcode;

    private Long shelfId;

    private Long branchId;

    private BookCondition condition = BookCondition.GOOD;

    private LocalDate acquisitionDate;

    private BigDecimal price;
}
