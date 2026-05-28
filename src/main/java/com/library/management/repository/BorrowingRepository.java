package com.library.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.library.management.entity.Borrowing;
import com.library.management.enums.BorrowingStatus;

import io.lettuce.core.dynamic.annotation.Param;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    // Finding all borrowings for a specific member with pagination
    Page<Borrowing> findAllByMemberId(Long memberId, Pageable pageable);

    // Counting active borrowings for a specific member
    int countByMemberIdAndStatus(Long memberId, BorrowingStatus status);

    long countByStatus(BorrowingStatus status);

    // Check if the member already borrowed the same book
    @Query("""
            SELECT COUNT(b) > 0 FROM Borrowing b
            WHERE b.member.id = :memberId
            AND b.bookCopy.book.id = :bookId
            AND b.status = 'ACTIVE'
            """)

    boolean existsActiveBorrowingForBook(
        @Param("memberId") Long memberId,
        @Param("bookId") Long bookId
    );

    // Get all overdue borrowings
    @Query("""
            SELECT b FROM Borrowing b
            JOIN FETCH b.member m
            JOIN FETCH m.user u
            JOIN FETCH b.bookCopy bc
            WHERE b.status = 'ACTIVE'
            AND b.dueDate < :now
            """)
    List<Borrowing> findAllOverdue(@Param("now") LocalDateTime now);

    // get all by status with pagination
    Page<Borrowing> findAllByStatus(BorrowingStatus status, Pageable pageable);

    // get active borrowings for a member with book details
    @Query("""
            SELECT b FROM Borrowing b
            JOIN FETCH b.bookCopy bc
            JOIN FETCH bc.book bk
            WHERE b.member.id = :memberId
            AND b.status = 'ACTIVE'
            """)
    List<Borrowing> findActiveBorrowingsByMemberId(@Param("memberId") Long memberId);

    // find borrowing by barcode that is active
    @Query("""
            SELECT b FROM Borrowing b
            WHERE b.bookCopy.barcode = :barcode
            AND b.status = 'ACTIVE'
            """)
    Optional<Borrowing> findActiveByBarcode(@Param("barcode") String barcode);
    
    // Today's stats
    @Query("""
        SELECT COUNT(b) FROM Borrowing b
        WHERE b.borrowDate >= :startOfDay
        AND b.borrowDate <= :endOfDay
        """)
    long countBorrowingsToday(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("""
        SELECT COUNT(b) FROM Borrowing b
        WHERE b.returnDate >= :startOfDay
        AND b.returnDate <= :endOfDay
        """)
    long countReturnsToday(
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );

    // Due soon (reminder)
    @Query("""
        SELECT b FROM Borrowing b
        JOIN FETCH b.member m
        JOIN FETCH m.user u
        JOIN FETCH b.bookCopy bc
        JOIN FETCH bc.book bk
        WHERE b.status = 'ACTIVE'
        AND b.dueDate BETWEEN :from AND :to
        """)
    List<Borrowing> findDueSoon(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    // Most borrowed books with count
    @Query("""
        SELECT bk.id as bookId, bk.title as title, bk.isbn as isbn,
               COUNT(b) as borrowCount
        FROM Borrowing b
        JOIN b.bookCopy bc
        JOIN bc.book bk
        WHERE b.borrowDate BETWEEN :startDate AND :endDate
        GROUP BY bk.id, bk.title, bk.isbn
        ORDER BY COUNT(b) DESC
        """)
    List<Object[]> findMostBorrowedBooks(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    // Active members report (members who borrowed in given period)
    @Query("""
        SELECT m.id as memberId, m.membershipNumber as memberNumber,
               u.firstName as firstName, u.lastName as lastName,
               u.email as email, COUNT(b) as borrowCount
        FROM Borrowing b
        JOIN b.member m
        JOIN m.user u
        WHERE b.borrowDate BETWEEN :startDate AND :endDate
        GROUP BY m.id, m.membershipNumber, u.firstName, u.lastName, u.email
        ORDER BY COUNT(b) DESC
        """)
    List<Object[]> findActiveMembers(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
}
