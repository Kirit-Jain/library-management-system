package com.library.management.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.library.management.enums.BookCondition;
import com.library.management.enums.BookCopyStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookLocationResponse {

    private Long bookId;
    private String isbn;
    private String title;
    private Integer totalCopies;
    private Integer availableCopies;
    private List<CopyDetail> copies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CopyDetail {
        private Long copyId;
        private String barcode;

        //location details
        private String branchName;
        private String branchAddress;
        private String floorName;
        private Integer floorNumber;
        private String sectionName;
        private String sectionCode;
        private String shelfCode;

        // status details
        private BookCopyStatus status;
        private BookCondition condition;

        // if currently borrowed
        private LocalDateTime expectedReturnDate;
        private String locationDescription;
    }
}
