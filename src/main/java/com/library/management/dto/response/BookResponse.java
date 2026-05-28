package com.library.management.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Long id;
    private String isbn;
    private String title;
    private String description;
    private String publisherName;
    private Integer publicationYear;
    private String edition;
    private String language;
    private Integer pageCount;
    private String coverImageUrl;
    private Integer totalCopies;
    private Integer availableCopies;
    private boolean available;
    private Set<String> authors;
    private Set<String> categories;
    private LocalDateTime createdAt;
}