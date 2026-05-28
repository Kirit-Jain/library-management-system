package com.library.management.service;

import com.library.management.dto.request.FinePaymentRequest;
import com.library.management.dto.response.FineResponse;
import com.library.management.entity.Fine;
import com.library.management.entity.FinePayment;
import com.library.management.entity.Member;
import com.library.management.entity.User;
import com.library.management.enums.FineStatus;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.FinePaymentRepository;
import com.library.management.repository.FineRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FineService {

    private final FineRepository fineRepository;
    private final FinePaymentRepository finePaymentRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    public Page<FineResponse> getAllFines(Pageable pageable) {
        return fineRepository.findAll(pageable).map(this::mapToResponse);
    }

    public Page<FineResponse> getUnpaidFines(Pageable pageable) {
        return fineRepository.findAllByStatus(FineStatus.UNPAID, pageable)
            .map(this::mapToResponse);
    }

    public FineResponse getFineById(Long id) {
        Fine fine = fineRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Fine", "id", id));
        return mapToResponse(fine);
    }

    public List<FineResponse> getMemberFines(Long memberId) {
        return fineRepository.findAllByMemberId(memberId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    public List<FineResponse> getMemberUnpaidFines(Long memberId) {
        return fineRepository.findAllByMemberIdAndStatus(memberId, FineStatus.UNPAID)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Transactional
    public FineResponse payFine(FinePaymentRequest request, Long receivedByUserId) {
        Fine fine = fineRepository.findById(request.getFineId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Fine", "id", request.getFineId()));

        if (fine.getStatus() == FineStatus.PAID) {
            throw new BadRequestException("Fine is already paid");
        }

        if (fine.getStatus() == FineStatus.WAIVED) {
            throw new BadRequestException("Fine has been waived");
        }

        BigDecimal remaining = fine.getRemainingAmount();

        if (request.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException(
                "Payment amount (rupee" + request.getAmount() +
                ") exceeds remaining fine (rupee" + remaining + ")");
        }

        // Create payment record
        FinePayment payment = FinePayment.builder()
            .fine(fine)
            .amount(request.getAmount())
            .paymentMethod(request.getPaymentMethod())
            .paymentDate(LocalDateTime.now())
            .transactionId(request.getTransactionId())
            .receivedBy(receivedByUserId)
            .build();

        finePaymentRepository.save(payment);

        // Update fine
        fine.setPaidAmount(fine.getPaidAmount().add(request.getAmount()));

        if (fine.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            fine.setStatus(FineStatus.PAID);
            fine.setPaidDate(LocalDate.now());
        }

        fineRepository.save(fine);

        // Update member's pending fines
        Member member = fine.getMember();
        BigDecimal newPending = fineRepository.getTotalUnpaidFines(member.getId());
        member.setTotalFinesPending(newPending);
        memberRepository.save(member);

        log.info("Fine paid → fineId: {}, amount: rupee{}, method: {}",
            fine.getId(), request.getAmount(), request.getPaymentMethod());

        return mapToResponse(fine);
    }

    @Transactional
    public FineResponse waiveFine(Long fineId, String reason) {
        Fine fine = fineRepository.findById(fineId)
            .orElseThrow(() -> new ResourceNotFoundException("Fine", "id", fineId));

        if (fine.getStatus() != FineStatus.UNPAID) {
            throw new BadRequestException(
                "Only unpaid fines can be waived. Current status: " + fine.getStatus());
        }

        fine.setStatus(FineStatus.WAIVED);
        fine.setNotes("WAIVED: " + reason);

        // Update member pending total
        Member member = fine.getMember();
        BigDecimal newPending = fineRepository.getTotalUnpaidFines(member.getId());
        member.setTotalFinesPending(newPending);
        memberRepository.save(member);

        log.info("Fine waived → fineId: {}, reason: {}", fineId, reason);
        return mapToResponse(fineRepository.save(fine));
    }

    public List<FineResponse> getMyFines(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        Member member = memberRepository.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Member", "userId", user.getId()));
        
        return fineRepository.findAllByMemberId(member.getId())
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    private FineResponse mapToResponse(Fine fine) {
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