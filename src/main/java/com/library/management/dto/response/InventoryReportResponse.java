package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReportResponse {
    private Long totalBooks;
    private Long totalCopies;
    private Long availableCopies;
    private Long borrowedCopies;
    private Long damagedCopies;
    private Long lostCopies;
    private List<CategoryInventory> byCategory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryInventory {
        private String categoryName;
        private Long bookCount;
        private Long copyCount;
    }
}