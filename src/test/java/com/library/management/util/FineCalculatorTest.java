package com.library.management.util;

import com.library.management.entity.Borrowing;
import com.library.management.enums.BorrowingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FineCalculator Unit Tests")
class FineCalculatorTest {

    private FineCalculator fineCalculator;

    @BeforeEach
    void setUp() {
        fineCalculator = new FineCalculator();
        ReflectionTestUtils.setField(fineCalculator, "finePerDay", new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("Should return zero fine for non-overdue borrowing")
    void calculateOverdueFine_ShouldReturnZero_WhenNotOverdue() {
        Borrowing borrowing = Borrowing.builder()
            .dueDate(LocalDateTime.now().plusDays(5))
            .status(BorrowingStatus.ACTIVE)
            .build();

        BigDecimal fine = fineCalculator.calculateOverdueFine(borrowing);

        assertThat(fine).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should calculate correct fine for 5 days overdue")
    void calculateOverdueFine_ShouldReturn50_ForFiveDaysOverdue() {
        Borrowing borrowing = Borrowing.builder()
            .dueDate(LocalDateTime.now().minusDays(5))
            .status(BorrowingStatus.ACTIVE)
            .build();

        BigDecimal fine = fineCalculator.calculateOverdueFine(borrowing);

        assertThat(fine).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Should calculate fine using due date directly")
    void calculateOverdueFine_FromDueDate_ShouldWork() {
        LocalDateTime dueDate = LocalDateTime.now().minusDays(3);

        BigDecimal fine = fineCalculator.calculateOverdueFine(dueDate);

        assertThat(fine).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Should return zero days for non-overdue")
    void getOverdueDays_ShouldReturnZero_WhenNotOverdue() {
        long days = fineCalculator.getOverdueDays(LocalDateTime.now().plusDays(2));

        assertThat(days).isZero();
    }
}