package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.LibraryBranch;
import com.library.management.repository.LibraryBranchRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
@Tag(name = "Branches", description = "Library branches")
public class LibraryBranchController {

    private final LibraryBranchRepository branchRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LibraryBranch>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Library branches retrieved successfully", branchRepository.findAll()));
    }
}