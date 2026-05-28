package com.library.management.repository;

import com.library.management.entity.Reservation;
import com.library.management.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // Check if book is reserved by anyone
    boolean existsByBookIdAndStatus(Long bookId, ReservationStatus status);

    // Check if member already reserved this book
    boolean existsByMemberIdAndBookIdAndStatus(
        Long memberId, Long bookId, ReservationStatus status);

    // Find all pending reservations for a book (oldest first)
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.book.id = :bookId
        AND r.status = 'PENDING'
        ORDER BY r.reservationDate ASC
        """)
    List<Reservation> findPendingByBookId(@Param("bookId") Long bookId);

    // Find reservation by member and book
    Optional<Reservation> findByMemberIdAndBookIdAndStatus(
        Long memberId, Long bookId, ReservationStatus status);

    // Find expired reservations
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.status = 'PENDING'
        AND r.expiryDate < :now
        """)
    List<Reservation> findExpiredReservations(@Param("now") LocalDateTime now);

    // All reservations for a member
    List<Reservation> findAllByMemberId(Long memberId);

    // Count pending
    long countByStatus(ReservationStatus status);
}