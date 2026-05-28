package com.library.management.service;

import com.library.management.dto.response.DashboardStatsResponse;
import com.library.management.enums.BookCopyStatus;
import com.library.management.enums.BorrowingStatus;
import com.library.management.enums.ReservationStatus;
import com.library.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MemberRepository memberRepository;
    private final BorrowingRepository borrowingRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    @Cacheable(value = "dashboard", key = "'stats'")
    public DashboardStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime now = LocalDateTime.now();

        return DashboardStatsResponse.builder()
            // Books
            .totalBooks(bookRepository.count())
            .totalBookCopies(bookCopyRepository.count())
            .availableCopies(bookCopyRepository
                .countByBookIdAndStatus(null, BookCopyStatus.AVAILABLE))
            .borrowedCopies(bookCopyRepository
                .countByBookIdAndStatus(null, BookCopyStatus.BORROWED))

            // Members
            .totalMembers(memberRepository.count())
            .activeMembers(memberRepository.countByIsActive(true))

            // Borrowings
            .activeBorrowings(borrowingRepository
                .countByMemberIdAndStatus(null, BorrowingStatus.ACTIVE))
            .overdueBorrowings((long) borrowingRepository
                .findAllOverdue(now).size())
            .borrowingsToday(borrowingRepository
                .countBorrowingsToday(startOfDay, endOfDay))
            .returnsToday(borrowingRepository
                .countReturnsToday(startOfDay, endOfDay))

            // Fines
            .totalUnpaidFines(fineRepository.getTotalUnpaidFinesAll())
            .totalCollectedFines(fineRepository.getTotalCollectedFines())

            // Reservations
            .pendingReservations(reservationRepository
                .countByStatus(ReservationStatus.PENDING))
            .build();
    }
}