package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MostBorrowedBookResponse {
    private Long bookId;
    private String title;
    private String isbn;
    private Long borrowCount;
    private Integer rank;
}