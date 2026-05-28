package com.library.management.repository;

import com.library.management.entity.BookCopy;
import com.library.management.enums.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {
    
    Optional<BookCopy> findByBarcode(String barcode);

    Boolean existsByBarcode(String barcode);

    List<BookCopy> findAllByBookId(Long bookId);

    List<BookCopy> findAllByBookIdAndStatus(Long bookId, BookCopyStatus status);
    
    @Query("""
            SELECT bc FROM BookCopy bc
            JOIN FETCH bc.book b
            LEFT JOIN FETCH bc.shelf s
            LEFT JOIN FETCH bc.branch br
            WHERE bc.book.id = :bookId
            """)
    List<BookCopy> findAllByBookIdWithDetails(@Param("bookId") Long bookId);
    
    Long countByBookIdAndStatus(Long bookId, BookCopyStatus status);

    long countByStatus(BookCopyStatus status);
}