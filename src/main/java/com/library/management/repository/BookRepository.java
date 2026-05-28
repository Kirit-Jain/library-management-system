package com.library.management.repository;

import com.library.management.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Boolean existsByIsbn(String isbn);

    // Search for books by title or author
    @Query("""
            SELECT DISTINCT b FROM Book b
            LEFT JOIN b.authors a
            WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.firstName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.lastName) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    
    Page<Book> searchBooks(@Param("query") String query, Pageable pageable);

    // Find books by category
    @Query("""
            SELECT b FROM Book b
            JOIN b.categories c
            WHERE c.id = :categoryId
            """)
    Page<Book> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // Available books
    @Query("""
            SELECT b FROM Book b
            WHERE b.availableCopies > 0
            """)
    Page<Book> findAvailableBooks(Pageable pageable);
}
