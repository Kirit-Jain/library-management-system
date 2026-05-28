package com.library.management.scheduler;

import com.library.management.entity.Borrowing;
import com.library.management.repository.BorrowingRepository;
import com.library.management.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OverdueReminderScheduler {

    private final BorrowingRepository borrowingRepository;
    private final NotificationService notificationService;

    // ============================================
    // RUNS EVERY DAY AT 9:00 AM
    // Sends overdue reminders to members
    // ============================================
    @Scheduled(cron = "0 0 9 * * *")
    public void sendOverdueReminders() {
        log.info("📧 Sending overdue reminders...");

        List<Borrowing> overdueBorrowings = borrowingRepository
            .findAllOverdue(LocalDateTime.now());

        int sentCount = 0;
        for (Borrowing borrowing : overdueBorrowings) {
            try {
                notificationService.sendOverdueReminder(borrowing);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send overdue reminder for borrowing {}",
                    borrowing.getId(), e);
            }
        }

        log.info("✅ Sent {} overdue reminders", sentCount);
    }

    // ============================================
    // RUNS EVERY DAY AT 10:00 AM
    // Sends "due tomorrow" reminders
    // ============================================
    @Scheduled(cron = "0 0 10 * * *")
    public void sendDueSoonReminders() {
        log.info("📧 Sending due-soon reminders...");

        LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = LocalDate.now().plusDays(1).atTime(23, 59, 59);

        List<Borrowing> dueSoon = borrowingRepository
            .findDueSoon(tomorrow, endOfTomorrow);

        int sentCount = 0;
        for (Borrowing borrowing : dueSoon) {
            try {
                notificationService.sendDueSoonReminder(borrowing);
                sentCount++;
            } catch (Exception e) {
                log.error("Failed to send due-soon reminder", e);
            }
        }

        log.info("✅ Sent {} due-soon reminders", sentCount);
    }
}