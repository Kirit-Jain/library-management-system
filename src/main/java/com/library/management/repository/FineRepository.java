package com.library.management.repository;

import com.library.management.entity.Fine;
import com.library.management.enums.FineStatus;
import com.library.management.enums.FineType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    // All fines for a member
    List<Fine> findAllByMemberId(Long memberId);

    Page<Fine> findAllByMemberId(Long memberId, Pageable pageable);

    // Unpaid fines for a member
    List<Fine> findAllByMemberIdAndStatus(Long memberId, FineStatus status);

    // Total unpaid fines for a member
    @Query("""
        SELECT COALESCE(SUM(f.amount - f.paidAmount), 0)
        FROM Fine f
        WHERE f.member.id = :memberId
        AND f.status = 'UNPAID'
        """)
    BigDecimal getTotalUnpaidFines(@Param("memberId") Long memberId);

    // Total collected fines (all time)
    @Query("""
        SELECT COALESCE(SUM(f.paidAmount), 0)
        FROM Fine f
        WHERE f.status = 'PAID'
        """)
    BigDecimal getTotalCollectedFines();

    // Total unpaid across all members
    @Query("""
        SELECT COALESCE(SUM(f.amount - f.paidAmount), 0)
        FROM Fine f
        WHERE f.status = 'UNPAID'
        """)
    BigDecimal getTotalUnpaidFinesAll();

    // Find fine by borrowing and type
    Optional<Fine> findByBorrowingIdAndFineType(Long borrowingId, FineType fineType);

    // Unpaid fines with pagination
    Page<Fine> findAllByStatus(FineStatus status, Pageable pageable);

    // Fine collection by date range
    @Query("""
        SELECT fp.paymentDate as date, SUM(fp.amount) as totalAmount,
               COUNT(fp) as paymentCount
        FROM FinePayment fp
        WHERE fp.paymentDate BETWEEN :startDate AND :endDate
        GROUP BY fp.paymentDate
        ORDER BY fp.paymentDate
        """)
    List<Object[]> getFineCollectionByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Total collected in period
    @Query("""
        SELECT COALESCE(SUM(fp.amount), 0)
        FROM FinePayment fp
        WHERE fp.paymentDate BETWEEN :startDate AND :endDate
        """)
    BigDecimal getTotalCollectedBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}