package com.library.management.controller;

import com.library.management.dto.response.ApiResponse;
import com.library.management.entity.Publisher;
import com.library.management.repository.PublisherRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
@Tag(name = "Publishers", description = "Publisher management")
public class PublisherController {

    private final PublisherRepository publisherRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Publisher>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Publishers found", publisherRepository.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<ApiResponse<Publisher>> create(@RequestBody Publisher publisher) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Publisher created", publisherRepository.save(publisher)));
    }
}