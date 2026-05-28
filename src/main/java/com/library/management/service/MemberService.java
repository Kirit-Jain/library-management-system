package com.library.management.service;

import com.library.management.dto.request.MemberCreateRequest;
import com.library.management.dto.request.MemberUpdateRequest;
import com.library.management.dto.response.MemberResponse;
import com.library.management.entity.Member;
import com.library.management.entity.User;
import com.library.management.enums.BorrowingStatus;
import com.library.management.enums.MembershipType;
import com.library.management.exception.BadRequestException;
import com.library.management.exception.DuplicateResourceException;
import com.library.management.exception.ResourceNotFoundException;
import com.library.management.repository.BorrowingRepository;
import com.library.management.repository.FineRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.repository.UserRepository;
import com.library.management.util.MembershipNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;
    private final FineRepository fineRepository;
    private final MembershipNumberGenerator numberGenerator;

    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable).map(this::mapToResponse);
    }

    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));
        return mapToResponse(member);
    }

    public MemberResponse getMemberByMembershipNumber(String number) {
        Member member = memberRepository.findByMembershipNumberWithUser(number)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Member", "membershipNumber", number));
        return mapToResponse(member);
    }

    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "User", "id", request.getUserId()));

        // Check if user already has membership
        if (memberRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new DuplicateResourceException(
                "User already has a membership: " + user.getEmail());
        }

        // Determine limits based on membership type
        int maxBooks = getMaxBooks(request.getMembershipType());
        int maxDays = getMaxDays(request.getMembershipType());

        Member member = Member.builder()
            .user(user)
            .membershipNumber(numberGenerator.generate())
            .membershipType(request.getMembershipType())
            .membershipStart(request.getMembershipStartDate())
            .membershipExpiry(request.getMembershipExpiryDate())
            .maxBooksAllowed(maxBooks)
            .maxBorrowDays(maxDays)
            .isActive(true)
            .build();

        Member saved = memberRepository.save(member);
        log.info("Member created: number={}, user={}",
            saved.getMembershipNumber(), user.getEmail());

        return mapToResponse(saved);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberUpdateRequest request) {
        Member member = memberRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Member", "id", id));

        if (request.getMembershipType() != null) {
            member.setMembershipType(request.getMembershipType());
            member.setMaxBooksAllowed(getMaxBooks(request.getMembershipType()));
            member.setMaxBorrowDays(getMaxDays(request.getMembershipType()));
        }
        if (request.getMembershipExpiryDate() != null) {
            member.setMembershipExpiry(request.getMembershipExpiryDate());
        }
        if (request.getIsActive() != null) {
            member.setIsActive(request.getIsActive());
        }
        if (request.getMaxBooksAllowed() != null) {
            member.setMaxBooksAllowed(request.getMaxBooksAllowed());
        }
        if (request.getMaxBorrowDays() != null) {
            member.setMaxBorrowDays(request.getMaxBorrowDays());
        }

        log.info("Member updated: id={}", id);
        return mapToResponse(memberRepository.save(member));
    }

    public MemberResponse getMyProfile(String email) {
        log.info("Looking for profile with email: {}", email);

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("User not found for email: {}", email);
                return new ResourceNotFoundException("User", "email", email);
            });

        log.info("Found user: id={}, email={}", user.getId(), user.getEmail());

        Member member = memberRepository.findByUserIdWithUser(user.getId())
            .orElseThrow(() -> {
                log.error("No member found for userId: {}", user.getId());
                return new ResourceNotFoundException(
                    "No membership found. Please contact librarian to create one.");
            });

        log.info("Found member: id={}, number={}", member.getId(), member.getMembershipNumber());
        return mapToResponse(member);
    }

    // ---- Private Helpers ----

    private int getMaxBooks(MembershipType type) {
        return switch (type) {
            case PREMIUM -> 10;
            case STUDENT -> 3;
            case SENIOR -> 7;
            default -> 5; // STANDARD
        };
    }

    private int getMaxDays(MembershipType type) {
        return switch (type) {
            case PREMIUM -> 21;
            case STUDENT -> 14;
            case SENIOR -> 21;
            default -> 14;
        };
    }

    public MemberResponse mapToResponse(Member member) {
        int currentBorrowed = (int) borrowingRepository
            .countByMemberIdAndStatus(member.getId(), BorrowingStatus.ACTIVE);
        
        BigDecimal totalFines = fineRepository.getTotalUnpaidFines(member.getId());
        if (totalFines == null) {
            totalFines = BigDecimal.ZERO;
        }
    
        User user = member.getUser();
    
        return MemberResponse.builder()
            .id(member.getId())
            .userId(user != null ? user.getId() : null)
            .membershipNumber(member.getMembershipNumber())
            .fullName(user != null ? user.getFullName() : "Unknown")
            .email(user != null ? user.getEmail() : "")
            .phone(user != null ? user.getPhone() : "")
            .membershipType(member.getMembershipType())
            .maxBooksAllowed(member.getMaxBooksAllowed())
            .maxBorrowDays(member.getMaxBorrowDays())
            .membershipStartDate(member.getMembershipStart())
            .membershipExpiryDate(member.getMembershipExpiry())
            .isActive(member.getIsActive())
            .membershipExpired(member.isMembershipExpired())
            .totalFinesPending(totalFines)
            .currentBorrowedBooksCount(currentBorrowed)
            .createdAt(member.getCreatedAt())
            .build();
    }
}