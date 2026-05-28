package com.library.management.repository;

import com.library.management.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    Page<Author> findByFirstNameContainingOrLastNameContaining(
        String firstName, String lastName, Pageable pageable);
}
