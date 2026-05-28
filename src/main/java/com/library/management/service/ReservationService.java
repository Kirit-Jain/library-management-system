package com.library.management.service;

import com.library.management.dto.request.ReservationRequest;
import com.library.management.dto.response.ReservationResponse;
import com.library.management.entity.Book;
import com.library.management.entity.Member;
import com.library.management.entity.Reservation;
import com.library.management.enums.BookCopyStatus;
import com.library.management.enums.ReservationStatus;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.BookCopyRepository;
import com.library.management.repository.BookRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    @Value("${library.reservation-expiry-days:3}")
    private int reservationExpiryDays;

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Member", "id", request.getMemberId()));

        Book book = bookRepository.findById(request.getBookId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Book", "id", request.getBookId()));

        // Check if book is actually available (no need to reserve if available)
        long availableCopies = bookCopyRepository
            .countByBookIdAndStatus(book.getId(), BookCopyStatus.AVAILABLE);

        if (availableCopies > 0) {
            throw new BadRequestException(
                "Book is currently available. No need to reserve - just borrow it directly.");
        }

        // Check member doesn't already have a reservation for this book
        boolean alreadyReserved = reservationRepository
            .existsByMemberIdAndBookIdAndStatus(
                member.getId(), book.getId(), ReservationStatus.PENDING);

        if (alreadyReserved) {
            throw new DuplicateResourceException(
                "Member already has a pending reservation for this book");
        }

        Reservation reservation = Reservation.builder()
            .member(member)
            .book(book)
            .reservationDate(LocalDateTime.now())
            .expiryDate(LocalDateTime.now().plusDays(reservationExpiryDays))
            .status(ReservationStatus.PENDING)
            .notified(false)
            .build();

        Reservation saved = reservationRepository.save(reservation);

        log.info("Reservation created → member: {}, book: {}",
            member.getMembershipNumber(), book.getTitle());

        return mapToResponse(saved);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Reservation", "id", reservationId));

        // Ensure member owns this reservation
        if (!reservation.getMember().getId().equals(memberId)) {
            throw new BadRequestException("You can only cancel your own reservations");
        }

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BadRequestException(
                "Only pending reservations can be cancelled. " +
                "Current status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        log.info("Reservation cancelled → id: {}", reservationId);
        return mapToResponse(reservation);
    }

    public List<ReservationResponse> getMemberReservations(Long memberId) {
        return reservationRepository.findAllByMemberId(memberId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
            .id(reservation.getId())
            .memberId(reservation.getMember().getId())
            .memberName(reservation.getMember().getUser().getFullName())
            .membershipNumber(reservation.getMember().getMembershipNumber())
            .bookId(reservation.getBook().getId())
            .bookTitle(reservation.getBook().getTitle())
            .isbn(reservation.getBook().getIsbn())
            .reservationDate(reservation.getReservationDate())
            .expiryDate(reservation.getExpiryDate())
            .status(reservation.getStatus())
            .notified(reservation.getNotified())
            .createdAt(reservation.getCreatedAt())
            .build();
    }
}