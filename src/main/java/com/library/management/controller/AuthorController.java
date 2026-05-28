package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.Author;
import com.library.management.repository.AuthorRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Author management")
public class AuthorController {

    private final AuthorRepository authorRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Author>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Authors found", authorRepository.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<Author>> create(@RequestBody Author author) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Author created", authorRepository.save(author)));
    }
}