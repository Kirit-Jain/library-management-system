package com.library.management.util;

import com.library.management.entity.Borrowing;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class FineCalculator {

    @Value("${library.fine-per-day:10.00}")
    private BigDecimal finePerDay;

    public BigDecimal calculateOverdueFine(Borrowing borrowing) {
        if (!borrowing.isOverdue()) {
            return BigDecimal.ZERO;
        }
        long overdueDays = ChronoUnit.DAYS.between(
            borrowing.getDueDate(),
            LocalDateTime.now()
        );
        return finePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    public BigDecimal calculateOverdueFine(LocalDateTime dueDate) {
        if (LocalDateTime.now().isBefore(dueDate)) {
            return BigDecimal.ZERO;
        }
        long overdueDays = ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
        return finePerDay.multiply(BigDecimal.valueOf(overdueDays));
    }

    public long getOverdueDays(LocalDateTime dueDate) {
        if (LocalDateTime.now().isBefore(dueDate)) return 0;
        return ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
    }
}