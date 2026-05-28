package com.library.management.service;

import com.library.management.dto.request.BorrowRequest;
import com.library.management.dto.response.BorrowingResponse;
import com.library.management.entity.*;
import com.library.management.enums.*;
import com.library.management.exception.*;
import com.library.management.repository.*;
import com.library.management.util.FineCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BorrowingService Unit Tests")
class BorrowingServiceTest {

    @Mock private BorrowingRepository borrowingRepository;
    @Mock private BookCopyRepository bookCopyRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private FineRepository fineRepository;
    @Mock private ReservationRepository reservationRepository;
    @Mock private FineCalculator fineCalculator;

    @InjectMocks
    private BorrowingService borrowingService;

    private Member testMember;
    private BookCopy testBookCopy;
    private Book testBook;
    private User testUser;
    private BorrowRequest borrowRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(borrowingService, "maxRenewals", 2);
        ReflectionTestUtils.setField(borrowingService, "finePerDay", new BigDecimal("10.00"));

        testUser = User.builder()
            .id(1L)
            .email("test@test.com")
            .firstName("Test")
            .lastName("User")
            .build();

        testMember = Member.builder()
            .id(1L)
            .user(testUser)
            .membershipNumber("LIB-2025-1000")
            .membershipType(MembershipType.STANDARD)
            .maxBooksAllowed(5)
            .maxBorrowDays(14)
            .isActive(true)
            .membershipStart(LocalDate.now().minusYears(1))
            .membershipExpiry(LocalDate.now().plusYears(1))
            .totalFinesPending(BigDecimal.ZERO)
            .build();

        testBook = Book.builder()
            .id(1L)
            .title("The Alchemist")
            .isbn("9780061122415")
            .availableCopies(2)
            .build();

        testBookCopy = BookCopy.builder()
            .id(1L)
            .book(testBook)
            .barcode("BC-001-ALCHEMIST")
            .status(BookCopyStatus.AVAILABLE)
            .build();

        borrowRequest = new BorrowRequest();
        borrowRequest.setMemberId(1L);
        borrowRequest.setBarcode("BC-001-ALCHEMIST");
    }

    @Test
    @DisplayName("Should borrow book successfully")
    void borrowBook_ShouldSucceed_WhenAllValidationsPass() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(bookCopyRepository.findByBarcode("BC-001-ALCHEMIST"))
            .thenReturn(Optional.of(testBookCopy));
        when(fineRepository.getTotalUnpaidFines(1L)).thenReturn(BigDecimal.ZERO);
        when(borrowingRepository.countByMemberIdAndStatus(1L, BorrowingStatus.ACTIVE))
            .thenReturn(0);
        when(borrowingRepository.existsActiveBorrowingForBook(1L, 1L)).thenReturn(false);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> {
            Borrowing b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });

        BorrowingResponse response = borrowingService.borrowBook(borrowRequest, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(BorrowingStatus.ACTIVE);
        assertThat(response.getBarcode()).isEqualTo("BC-001-ALCHEMIST");
        verify(bookCopyRepository, times(1)).save(any(BookCopy.class));
        assertThat(testBookCopy.getStatus()).isEqualTo(BookCopyStatus.BORROWED);
    }

    @Test
    @DisplayName("Should fail when member is inactive")
    void borrowBook_ShouldFail_WhenMemberIsInactive() {
        testMember.setIsActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest, 1L))
            .isInstanceOf(BadRequestException.class)
            .hasMessageContaining("inactive");
    }

    @Test
    @DisplayName("Should fail when membership expired")
    void borrowBook_ShouldFail_WhenMembershipExpired() {
        testMember.setMembershipExpiry(LocalDate.now().minusDays(1));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest, 1L))
            .isInstanceOf(MembershipExpiredException.class)
            .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Should fail when borrowing limit reached")
    void borrowBook_ShouldFail_WhenBorrowLimitReached() {
        // Setup base expectations
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(borrowingRepository.countByMemberIdAndStatus(1L, BorrowingStatus.ACTIVE)).thenReturn(5);
        
        // Handle variations in validation ordering safely via lenient staging
        lenient().when(fineRepository.getTotalUnpaidFines(1L)).thenReturn(BigDecimal.ZERO);
        lenient().when(bookCopyRepository.findByBarcode("BC-001-ALCHEMIST")).thenReturn(Optional.of(testBookCopy));

        // Act & Assert
        assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest, 1L))
            .isInstanceOf(BorrowLimitExceededException.class)
            .hasMessageContaining("limit reached");
    }

    @Test
    @DisplayName("Should fail when unpaid fines exceed limit")
    void borrowBook_ShouldFail_WhenUnpaidFinesExceedLimit() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(fineRepository.getTotalUnpaidFines(1L))
            .thenReturn(new BigDecimal("600.00"));

        assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest, 1L))
            .isInstanceOf(FineUnpaidException.class)
            .hasMessageContaining("unpaid fines");
    }

    @Test
    @DisplayName("Should fail when book copy is not available")
    void borrowBook_ShouldFail_WhenBookCopyNotAvailable() {
        testBookCopy.setStatus(BookCopyStatus.BORROWED);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(bookCopyRepository.findByBarcode("BC-001-ALCHEMIST"))
            .thenReturn(Optional.of(testBookCopy));

        assertThatThrownBy(() -> borrowingService.borrowBook(borrowRequest, 1L))
            .isInstanceOf(BookNotAvailableException.class)
            .hasMessageContaining("not available");
    }
}