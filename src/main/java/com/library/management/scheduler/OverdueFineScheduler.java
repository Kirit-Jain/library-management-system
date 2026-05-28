package com.library.management.scheduler;

import com.library.management.entity.Borrowing;
import com.library.management.entity.Fine;
import com.library.management.entity.Member;
import com.library.management.enums.BorrowingStatus;
import com.library.management.enums.FineStatus;
import com.library.management.enums.FineType;
import com.library.management.repository.BorrowingRepository;
import com.library.management.repository.FineRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.util.FineCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueFineScheduler {

    private final BorrowingRepository borrowingRepository;
    private final FineRepository fineRepository;
    private final MemberRepository memberRepository;
    private final FineCalculator fineCalculator;

    // ============================================
    // RUNS EVERY DAY AT 1:00 AM
    // Calculates fines for all overdue books
    // ============================================
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void calculateDailyOverdueFines() {
        log.info("════════════════════════════════════════");
        log.info("🕐 Starting daily overdue fine calculation");
        log.info("════════════════════════════════════════");

        List<Borrowing> overdueBorrowings = borrowingRepository
            .findAllOverdue(LocalDateTime.now());

        int processed = 0;
        int newFines = 0;
        int updatedFines = 0;

        for (Borrowing borrowing : overdueBorrowings) {
            // Mark as overdue
            if (borrowing.getStatus() == BorrowingStatus.ACTIVE) {
                borrowing.setStatus(BorrowingStatus.OVERDUE);
                borrowingRepository.save(borrowing);
            }

            BigDecimal fineAmount = fineCalculator.calculateOverdueFine(borrowing);

            if (fineAmount.compareTo(BigDecimal.ZERO) <= 0) continue;

            Optional<Fine> existingFine = fineRepository
                .findByBorrowingIdAndFineType(borrowing.getId(), FineType.OVERDUE);

            if (existingFine.isPresent()) {
                // Update existing unpaid fine
                Fine fine = existingFine.get();
                if (fine.getStatus() == FineStatus.UNPAID) {
                    fine.setAmount(fineAmount);
                    fine.setNotes("Updated: Overdue by " +
                        borrowing.getOverdueDays() + " days");
                    fineRepository.save(fine);
                    updatedFines++;
                }
            } else {
                // Create new fine
                Fine fine = Fine.builder()
                    .member(borrowing.getMember())
                    .borrowing(borrowing)
                    .amount(fineAmount)
                    .fineType(FineType.OVERDUE)
                    .fineDate(LocalDate.now())
                    .dueDate(LocalDate.now().plusDays(30))
                    .status(FineStatus.UNPAID)
                    .notes("Overdue by " + borrowing.getOverdueDays() + " days")
                    .build();
                fineRepository.save(fine);
                newFines++;
            }

            // Update member's pending fines total
            Member member = borrowing.getMember();
            BigDecimal totalPending = fineRepository.getTotalUnpaidFines(member.getId());
            member.setTotalFinesPending(totalPending);
            memberRepository.save(member);

            processed++;
        }

        log.info("✅ Overdue fine calculation complete");
        log.info("   Processed: {} borrowings", processed);
        log.info("   New fines: {}", newFines);
        log.info("   Updated fines: {}", updatedFines);
    }
}