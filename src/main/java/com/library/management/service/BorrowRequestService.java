package com.library.management.service;

import com.library.management.dto.response.BorrowRequestResponse;
import com.library.management.entity.*;
import com.library.management.enums.BookCopyStatus;
import com.library.management.enums.BorrowRequestStatus;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingService borrowingService;

    @Transactional
    public BorrowRequestResponse createRequest(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResourceNotFoundException("Member", "id", memberId));

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));

        BorrowRequest request = BorrowRequest.builder()
            .member(member)
            .book(book)
            .status(BorrowRequestStatus.PENDING)
            .build();

        BorrowRequest saved = borrowRequestRepository.save(request);

        log.info("Borrow request created: member={}, book={}",
            member.getMembershipNumber(), book.getTitle());

        return mapToResponse(saved);
    }

    public Page<BorrowRequestResponse> getPendingRequests(Pageable pageable) {
        return borrowRequestRepository
            .findAllByStatusOrderByCreatedAtDesc(BorrowRequestStatus.PENDING, pageable)
            .map(this::mapToResponse);
    }

    public List<BorrowRequestResponse> getMemberRequests(Long memberId) {
        return borrowRequestRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public BorrowRequestResponse approveRequest(Long requestId, Long approverId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BorrowRequest", "id", requestId));

        if (request.getStatus() != BorrowRequestStatus.PENDING) {
            throw new BadRequestException("Request is not pending");
        }

        List<BookCopy> availableCopies = bookCopyRepository
            .findAllByBookIdAndStatus(request.getBook().getId(), BookCopyStatus.AVAILABLE);

        if (availableCopies.isEmpty()) {
            throw new BadRequestException("No available copies of this book");
        }

        BookCopy copy = availableCopies.get(0);

        var borrowReq = new com.library.management.dto.request.BorrowRequest();
        borrowReq.setMemberId(request.getMember().getId());
        borrowReq.setBarcode(copy.getBarcode());
        borrowingService.borrowBook(borrowReq, approverId);

        request.setStatus(BorrowRequestStatus.APPROVED);
        request.setApprovedBy(approverId);
        request.setApprovedAt(LocalDateTime.now());

        BorrowRequest saved = borrowRequestRepository.save(request);

        log.info("Borrow request approved: id={}", requestId);
        return mapToResponse(saved);
    }

    @Transactional
    public BorrowRequestResponse rejectRequest(Long requestId, Long approverId, String reason) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("BorrowRequest", "id", requestId));

        if (request.getStatus() != BorrowRequestStatus.PENDING) {
            throw new BadRequestException("Request is not pending");
        }

        request.setStatus(BorrowRequestStatus.REJECTED);
        request.setApprovedBy(approverId);
        request.setApprovedAt(LocalDateTime.now());
        request.setRejectionReason(reason);

        BorrowRequest saved = borrowRequestRepository.save(request);

        log.info("Borrow request rejected: id={}, reason={}", requestId, reason);
        return mapToResponse(saved);
    }

    // ============================================
    //          MAPPER
    // ============================================
    private BorrowRequestResponse mapToResponse(BorrowRequest request) {
        Member member = request.getMember();
        User user = member.getUser();
        Book book = request.getBook();

        return BorrowRequestResponse.builder()
            .id(request.getId())
            .memberId(member.getId())
            .memberName(user != null ? user.getFullName() : "Unknown")
            .membershipNumber(member.getMembershipNumber())
            .memberEmail(user != null ? user.getEmail() : "")
            .bookId(book.getId())
            .bookTitle(book.getTitle())
            .bookIsbn(book.getIsbn())
            .status(request.getStatus())
            .approvedBy(request.getApprovedBy())
            .approvedAt(request.getApprovedAt())
            .rejectionReason(request.getRejectionReason())
            .createdAt(request.getCreatedAt())
            .build();
    }
}