package com.library.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class BookCreateRequest {

    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 13, message = "ISBN must be 10 or 13 characters")
    private String isbn;

    @NotBlank(message = "Title is required")
    @Size(max = 300, message = "Title too long")
    private String title;

    private String description;

    private Long publisherId;

    @Min(value = 1000, message = "Publication year seems too old")
    @Max(value = 2100, message = "Publication year seems invalid")
    private Integer publicationYear;

    private String edition;
    private String language;
    private Integer pageCount;
    private String coverImageUrl;

    private Set<Long> authorIds;
    private Set<Long> categoryIds;
}