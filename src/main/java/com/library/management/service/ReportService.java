package com.library.management.service;

import com.library.management.dto.response.*;
import com.library.management.entity.Borrowing;
import com.library.management.enums.BookCopyStatus;
import com.library.management.repository.*;
import com.library.management.util.FineCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final BorrowingRepository borrowingRepository;
    private final FineRepository fineRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final FineCalculator fineCalculator;

    // ============================================
    //          MOST BORROWED BOOKS
    // ============================================
    public List<MostBorrowedBookResponse> getMostBorrowedBooks(
        LocalDate startDate, LocalDate endDate, int limit) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = borrowingRepository
            .findMostBorrowedBooks(start, end, PageRequest.of(0, limit));

        AtomicInteger rank = new AtomicInteger(1);
        return results.stream()
            .map(row -> MostBorrowedBookResponse.builder()
                .bookId((Long) row[0])
                .title((String) row[1])
                .isbn((String) row[2])
                .borrowCount((Long) row[3])
                .rank(rank.getAndIncrement())
                .build())
            .toList();
    }

    // ============================================
    //          ACTIVE MEMBERS REPORT
    // ============================================
    public List<ActiveMemberResponse> getActiveMembers(
        LocalDate startDate, LocalDate endDate, int limit) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Object[]> results = borrowingRepository
            .findActiveMembers(start, end, PageRequest.of(0, limit));

        AtomicInteger rank = new AtomicInteger(1);
        return results.stream()
            .map(row -> ActiveMemberResponse.builder()
                .memberId((Long) row[0])
                .membershipNumber((String) row[1])
                .fullName(row[2] + " " + row[3])
                .email((String) row[4])
                .borrowCount((Long) row[5])
                .rank(rank.getAndIncrement())
                .build())
            .toList();
    }

    // ============================================
    //          OVERDUE REPORT
    // ============================================
    public OverdueReportResponse getOverdueReport() {
        List<Borrowing> overdueBorrowings = borrowingRepository
            .findAllOverdue(LocalDateTime.now());

        BigDecimal totalFines = BigDecimal.ZERO;
        List<OverdueReportResponse.OverdueItem> items = new ArrayList<>();

        for (Borrowing b : overdueBorrowings) {
            BigDecimal fine = fineCalculator.calculateOverdueFine(b);
            totalFines = totalFines.add(fine);

            items.add(OverdueReportResponse.OverdueItem.builder()
                .borrowingId(b.getId())
                .memberName(b.getMember().getUser().getFullName())
                .membershipNumber(b.getMember().getMembershipNumber())
                .memberEmail(b.getMember().getUser().getEmail())
                .bookTitle(b.getBookCopy().getBook().getTitle())
                .barcode(b.getBookCopy().getBarcode())
                .borrowDate(b.getBorrowDate())
                .dueDate(b.getDueDate())
                .overdueDays(b.getOverdueDays())
                .estimatedFine(fine)
                .build());
        }

        return OverdueReportResponse.builder()
            .totalOverdueBorrowings(items.size())
            .totalOverdueFines(totalFines)
            .overdueList(items)
            .build();
    }

    // ============================================
    //          FINE COLLECTION REPORT
    // ============================================
    public FineCollectionReportResponse getFineCollectionReport(
        LocalDate startDate, LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        BigDecimal totalCollected = fineRepository
            .getTotalCollectedBetween(start, end);

        List<Object[]> dailyData = fineRepository
            .getFineCollectionByDateRange(start, end);

        List<FineCollectionReportResponse.DailyCollection> dailyList = dailyData.stream()
            .map(row -> FineCollectionReportResponse.DailyCollection.builder()
                .date(((LocalDateTime) row[0]).toLocalDate())
                .amount((BigDecimal) row[1])
                .paymentCount(((Long) row[2]).intValue())
                .build())
            .toList();

        int totalPayments = dailyList.stream()
            .mapToInt(FineCollectionReportResponse.DailyCollection::getPaymentCount)
            .sum();

        return FineCollectionReportResponse.builder()
            .startDate(startDate)
            .endDate(endDate)
            .totalCollected(totalCollected)
            .totalPayments(totalPayments)
            .dailyCollections(dailyList)
            .build();
    }

    // ============================================
    //          INVENTORY REPORT
    // ============================================
    public InventoryReportResponse getInventoryReport() {
        long totalBooks = bookRepository.count();
        long totalCopies = bookCopyRepository.count();
        long available = bookCopyRepository.countByStatus(BookCopyStatus.AVAILABLE);
        long borrowed = bookCopyRepository.countByStatus(BookCopyStatus.BORROWED);
        long lost = bookCopyRepository.countByStatus(BookCopyStatus.LOST);
        long maintenance = bookCopyRepository.countByStatus(BookCopyStatus.MAINTENANCE);

        return InventoryReportResponse.builder()
            .totalBooks(totalBooks)
            .totalCopies(totalCopies)
            .availableCopies(available)
            .borrowedCopies(borrowed)
            .damagedCopies(maintenance)
            .lostCopies(lost)
            .build();
    }
}