package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.Shelf;
import com.library.management.repository.ShelfRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shelves")
@RequiredArgsConstructor
@Tag(name = "Shelves", description = "Library shelves")
public class ShelfController {

    private final ShelfRepository shelfRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Shelf>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Shelves retrieved successfully", shelfRepository.findAll()));
    }
}