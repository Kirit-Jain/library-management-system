package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.Category;
import com.library.management.repository.CategoryRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Categories found", categoryRepository.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Category created", categoryRepository.save(category)));
    }
}