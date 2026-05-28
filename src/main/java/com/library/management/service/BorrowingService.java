package com.library.management.service;

import com.library.management.dto.request.BorrowRequest;
import com.library.management.dto.request.ReturnRequest;
import com.library.management.dto.response.BorrowingResponse;
import com.library.management.dto.response.FineResponse;
import com.library.management.dto.response.MemberBorrowingHistoryResponse;
import com.library.management.entity.*;
import com.library.management.enums.*;
import com.library.management.exception.*;
import com.library.management.repository.*;
import com.library.management.util.FineCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookCopyRepository bookCopyRepository;
    private final MemberRepository memberRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final FineCalculator fineCalculator;

    @Value("${library.max-renewals:2}")
    private int maxRenewals;

    @Value("${library.fine-per-day:10.00}")
    private BigDecimal finePerDay;

    // =============================================
    //              BORROW A BOOK
    // =============================================
    @Transactional
    public BorrowingResponse borrowBook(BorrowRequest request, Long issuedByUserId) {
        // 1. Get and validate member
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Member", "id", request.getMemberId()));

        validateMemberCanBorrow(member);

        // 2. Get and validate book copy by barcode
        BookCopy bookCopy = bookCopyRepository.findByBarcode(request.getBarcode())
            .orElseThrow(() -> new ResourceNotFoundException(
                "BookCopy", "barcode", request.getBarcode()));

        if (bookCopy.getStatus() != BookCopyStatus.AVAILABLE) {
            throw new BookNotAvailableException(
                "Book copy [" + request.getBarcode() + "] is not available. " +
                "Current status: " + bookCopy.getStatus());
        }

        // 3. Check member doesn't already have this book
        boolean alreadyBorrowed = borrowingRepository
            .existsActiveBorrowingForBook(
                member.getId(),
                bookCopy.getBook().getId()
            );

        if (alreadyBorrowed) {
            throw new BadRequestException(
                "Member already has a copy of this book borrowed");
        }

        // 4. Create borrowing record
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dueDate = now.plusDays(member.getMaxBorrowDays());

        Borrowing borrowing = Borrowing.builder()
            .member(member)
            .bookCopy(bookCopy)
            .borrowDate(now)
            .dueDate(dueDate)
            .status(BorrowingStatus.ACTIVE)
            .renewedCount(0)
            .issuedBy(issuedByUserId)
            .notes(request.getNotes())
            .build();

        // 5. Update book copy status
        bookCopy.setStatus(BookCopyStatus.BORROWED);
        bookCopyRepository.save(bookCopy);

        // 6. Update book available count
        Book book = bookCopy.getBook();
        book.setAvailableCopies(Math.max(0, book.getAvailableCopies() - 1));

        // 7. Save borrowing
        Borrowing saved = borrowingRepository.save(borrowing);

        log.info("Book borrowed → member: {}, barcode: {}, dueDate: {}",
            member.getMembershipNumber(), request.getBarcode(), dueDate);

        return mapToResponse(saved);
    }

    // =============================================
    //              RETURN A BOOK
    // =============================================
    @Transactional
    public BorrowingResponse returnBook(ReturnRequest request, Long returnedToUserId) {
        Borrowing borrowing = borrowingRepository.findById(request.getBorrowingId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Borrowing", "id", request.getBorrowingId()));

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new BadRequestException("This book has already been returned");
        }

        LocalDateTime now = LocalDateTime.now();

        // ✅ Capture overdue status BEFORE changing status/returnDate
        boolean wasOverdue = now.isAfter(borrowing.getDueDate());
        long overdueDays = wasOverdue
            ? ChronoUnit.DAYS.between(borrowing.getDueDate(), now)
            : 0;

        // 1. Mark as returned
        borrowing.setReturnDate(now);
        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setReturnedTo(returnedToUserId);

        if (request.getNotes() != null) {
            borrowing.setNotes(request.getNotes());
        }

        // 2. Update book copy status and condition
        BookCopy bookCopy = borrowing.getBookCopy();
        bookCopy.setStatus(BookCopyStatus.AVAILABLE);

        if (request.getCondition() != null) {
            bookCopy.setCondition(request.getCondition());
        }
        bookCopyRepository.save(bookCopy);

        // 3. Update book available count
        Book book = bookCopy.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        // 4. ✅ Calculate overdue fine using captured flag
        if (wasOverdue) {
            createOverdueFine(borrowing, overdueDays);
        }

        // 5. Create damaged fine if book came back damaged
        if (request.getCondition() == BookCondition.DAMAGED) {
            createDamageFine(borrowing, bookCopy);
        }

        // 6. Check if anyone has reserved this book
        checkAndFulfillReservation(book.getId());

        Borrowing saved = borrowingRepository.save(borrowing);

        log.info("Book returned → borrowingId: {}, wasOverdue: {}, overdueDays: {}",
            saved.getId(), wasOverdue, overdueDays);

        return mapToResponse(saved);
    }

    // =============================================
    //              RENEW A BOOK
    // =============================================
    @Transactional
    public BorrowingResponse renewBook(Long borrowingId) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Borrowing", "id", borrowingId));

        // Validate
        if (borrowing.getStatus() != BorrowingStatus.ACTIVE) {
            throw new BadRequestException("Only active borrowings can be renewed");
        }

        if (borrowing.getRenewedCount() >= maxRenewals) {
            throw new BadRequestException(
                "Maximum renewals (" + maxRenewals + ") reached for this borrowing");
        }

        // Check book is not reserved by someone else
        boolean isReserved = reservationRepository.existsByBookIdAndStatus(
            borrowing.getBookCopy().getBook().getId(),
            ReservationStatus.PENDING
        );

        if (isReserved) {
            throw new BadRequestException(
                "Cannot renew - another member has reserved this book");
        }

        // Extend due date from NOW (not from old due date)
        int borrowDays = borrowing.getMember().getMaxBorrowDays();
        borrowing.setDueDate(LocalDateTime.now().plusDays(borrowDays));
        borrowing.setRenewedCount(borrowing.getRenewedCount() + 1);

        Borrowing saved = borrowingRepository.save(borrowing);

        log.info("Book renewed → borrowingId: {}, renewCount: {}, newDueDate: {}",
            saved.getId(), saved.getRenewedCount(), saved.getDueDate());

        return mapToResponse(saved);
    }

    // =============================================
    //              GET OPERATIONS
    // =============================================

    public Page<BorrowingResponse> getAllBorrowings(Pageable pageable) {
        return borrowingRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<BorrowingResponse> getBorrowingsByStatus(
        BorrowingStatus status, Pageable pageable) {
        return borrowingRepository.findAllByStatus(status, pageable)
            .map(this::mapToResponse);
    }

    public List<BorrowingResponse> getOverdueBorrowings() {
        return borrowingRepository.findAllOverdue(LocalDateTime.now())
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    public MemberBorrowingHistoryResponse getMemberHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        List<Borrowing> active = borrowingRepository
            .findActiveBorrowingsByMemberId(memberId);

        List<Borrowing> allHistory = borrowingRepository
            .findAllByMemberId(memberId, Pageable.unpaged())
            .getContent();

        long overdueCount = allHistory.stream()
            .filter(Borrowing::isOverdue)
            .count();

        long returnedCount = allHistory.stream()
            .filter(b -> b.getStatus() == BorrowingStatus.RETURNED)
            .count();

        List<Fine> pendingFines = fineRepository
            .findAllByMemberIdAndStatus(memberId, FineStatus.UNPAID);

        BigDecimal totalFinesPending = fineRepository
            .getTotalUnpaidFines(memberId);

        BigDecimal totalFinesPaid = fineRepository
            .findAllByMemberId(memberId)
            .stream()
            .filter(f -> f.getStatus() == FineStatus.PAID)
            .map(Fine::getPaidAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MemberBorrowingHistoryResponse.builder()
            .memberId(member.getId())
            .memberName(member.getUser().getFullName())
            .membershipNumber(member.getMembershipNumber())
            .totalBorrowed(allHistory.size())
            .currentlyBorrowed(active.size())
            .totalOverdue((int) overdueCount)
            .totalReturned((int) returnedCount)
            .totalFinesPaid(totalFinesPaid)
            .totalFinesPending(totalFinesPending)
            .activeBorrowings(active.stream().map(this::mapToResponse).toList())
            .borrowingHistory(allHistory.stream().map(this::mapToResponse).toList())
            .pendingFines(pendingFines.stream().map(this::mapFineToResponse).toList())
            .build();
    }

    public List<BorrowingResponse> getMyBorrowings(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        Member member = memberRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Member", "userId", user.getId()));

        return borrowingRepository.findAllByMemberId(member.getId(), Pageable.unpaged())
            .getContent()
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // =============================================
    //           PRIVATE HELPERS
    // =============================================

    private void validateMemberCanBorrow(Member member) {
        if (!member.getIsActive()) {
            throw new BadRequestException("Member account is inactive");
        }

        if (member.isMembershipExpired()) {
            throw new MembershipExpiredException(
                "Membership expired on " + member.getMembershipExpiry() +
                ". Please renew your membership.");
        }

        BigDecimal unpaidFines = fineRepository.getTotalUnpaidFines(member.getId());
        if (unpaidFines != null && unpaidFines.compareTo(new BigDecimal("500.00")) > 0) {
            throw new FineUnpaidException(
                "Member has unpaid fines of " + unpaidFines +
                " rupees. Please clear fines before borrowing.");
        }

        long activeCount = borrowingRepository
            .countByMemberIdAndStatus(member.getId(), BorrowingStatus.ACTIVE);

        if (activeCount >= member.getMaxBooksAllowed()) {
            throw new BorrowLimitExceededException(
                "Borrowing limit reached: " + activeCount +
                "/" + member.getMaxBooksAllowed() +
                " books. Return a book to borrow another.");
        }
    }

    // ✅ Updated to accept precomputed overdueDays
    private void createOverdueFine(Borrowing borrowing, long overdueDays) {
        // Check if fine already exists for this borrowing
        boolean fineExists = fineRepository
            .findByBorrowingIdAndFineType(borrowing.getId(), FineType.OVERDUE)
            .isPresent();

        if (fineExists) return;

        BigDecimal fineAmount = fineCalculator.calculateOverdueFine(borrowing.getDueDate());

        if (fineAmount.compareTo(BigDecimal.ZERO) <= 0) return;

        Fine fine = Fine.builder()
            .member(borrowing.getMember())
            .borrowing(borrowing)
            .amount(fineAmount)
            .fineType(FineType.OVERDUE)
            .fineDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30))
            .status(FineStatus.UNPAID)
            .notes("Overdue by " + overdueDays + " days")
            .build();

        fineRepository.save(fine);

        // Update member's pending fines total
        Member member = borrowing.getMember();
        member.setTotalFinesPending(
            member.getTotalFinesPending().add(fineAmount));
        memberRepository.save(member);

        log.info("Overdue fine created → member: {}, amount: rupee{}, days: {}",
            member.getMembershipNumber(), fineAmount, overdueDays);
    }

    private void createDamageFine(Borrowing borrowing, BookCopy bookCopy) {
        BigDecimal damageAmount = bookCopy.getPrice() != null
            ? bookCopy.getPrice().multiply(new BigDecimal("0.5"))
            : new BigDecimal("500");

        Fine fine = Fine.builder()
            .member(borrowing.getMember())
            .borrowing(borrowing)
            .amount(damageAmount)
            .fineType(FineType.DAMAGED_BOOK)
            .fineDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30))
            .status(FineStatus.UNPAID)
            .notes("Book returned in damaged condition")
            .build();

        fineRepository.save(fine);
        log.info("Damage fine created → amount: {}", damageAmount);
    }

    private void checkAndFulfillReservation(Long bookId) {
        List<Reservation> pending = reservationRepository
            .findPendingByBookId(bookId);

        if (!pending.isEmpty()) {
            Reservation first = pending.get(0);
            first.setStatus(ReservationStatus.FULFILLED);
            reservationRepository.save(first);
            log.info("Reservation fulfilled → member: {}, book: {}",
                first.getMember().getMembershipNumber(), bookId);
        }
    }

    // =============================================
    //              MAPPERS
    // =============================================

    public BorrowingResponse mapToResponse(Borrowing borrowing) {
        return BorrowingResponse.builder()
            .id(borrowing.getId())
            .memberId(borrowing.getMember().getId())
            .memberName(borrowing.getMember().getUser().getFullName())
            .membershipNumber(borrowing.getMember().getMembershipNumber())
            .bookCopyId(borrowing.getBookCopy().getId())
            .barcode(borrowing.getBookCopy().getBarcode())
            .bookTitle(borrowing.getBookCopy().getBook().getTitle())
            .bookIsbn(borrowing.getBookCopy().getBook().getIsbn())
            .borrowDate(borrowing.getBorrowDate())
            .dueDate(borrowing.getDueDate())
            .returnDate(borrowing.getReturnDate())
            .renewedCount(borrowing.getRenewedCount())
            .status(borrowing.getStatus())
            .isOverdue(borrowing.isOverdue())
            .overdueDays(borrowing.getOverdueDays())
            .notes(borrowing.getNotes())
            .createdAt(borrowing.getCreatedAt())
            .build();
    }

    private FineResponse mapFineToResponse(Fine fine) {
        return FineResponse.builder()
            .id(fine.getId())
            .memberId(fine.getMember().getId())
            .memberName(fine.getMember().getUser().getFullName())
            .membershipNumber(fine.getMember().getMembershipNumber())
            .borrowingId(fine.getBorrowing().getId())
            .bookTitle(fine.getBorrowing().getBookCopy().getBook().getTitle())
            .barcode(fine.getBorrowing().getBookCopy().getBarcode())
            .amount(fine.getAmount())
            .paidAmount(fine.getPaidAmount())
            .remainingAmount(fine.getRemainingAmount())
            .fineType(fine.getFineType())
            .status(fine.getStatus())
            .fineDate(fine.getFineDate())
            .dueDate(fine.getDueDate())
            .paidDate(fine.getPaidDate())
            .notes(fine.getNotes())
            .createdAt(fine.getCreatedAt())
            .build();
    }
}