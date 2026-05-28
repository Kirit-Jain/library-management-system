package com.library.management.service;

import com.library.management.dto.response.NotificationResponse;
import com.library.management.entity.Borrowing;
import com.library.management.entity.Notification;
import com.library.management.entity.User;
import com.library.management.enums.NotificationType;
import com.library.management.repository.NotificationRepository;
import com.library.management.util.FineCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final FineCalculator fineCalculator;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ============================================
    //          SEND OVERDUE REMINDER
    // ============================================
    @Transactional
    public void sendOverdueReminder(Borrowing borrowing) {
        User user = borrowing.getMember().getUser();
        String bookTitle = borrowing.getBookCopy().getBook().getTitle();
        long daysOverdue = borrowing.getOverdueDays();
        var fineAmount = fineCalculator.calculateOverdueFine(borrowing);

        saveNotification(
            user,
            "Overdue Book: " + bookTitle,
            String.format(
                "Your borrowed book '%s' is overdue by %d days. " +
                "Current fine: ₹%s. Please return it ASAP.",
                bookTitle, daysOverdue, fineAmount
            ),
            NotificationType.OVERDUE_REMINDER
        );

        try {
            emailService.sendOverdueReminder(
                user.getEmail(),
                user.getFullName(),
                bookTitle,
                daysOverdue,
                fineAmount.toString()
            );
        } catch (Exception e) {
            log.error("Failed to send overdue email", e);
        }

        log.info("Overdue reminder sent → user: {}, book: {}",
            user.getEmail(), bookTitle);
    }

    // ============================================
    //          SEND DUE SOON REMINDER
    // ============================================
    @Transactional
    public void sendDueSoonReminder(Borrowing borrowing) {
        User user = borrowing.getMember().getUser();
        String bookTitle = borrowing.getBookCopy().getBook().getTitle();
        String dueDateStr = borrowing.getDueDate().format(DATE_FORMATTER);

        saveNotification(
            user,
            "Book Due Soon: " + bookTitle,
            String.format("Your book '%s' is due on %s. " +
                          "Please return or renew it.", bookTitle, dueDateStr),
            NotificationType.DUE_SOON_REMINDER
        );

        try {
            emailService.sendDueSoonReminder(
                user.getEmail(),
                user.getFullName(),
                bookTitle,
                dueDateStr
            );
        } catch (Exception e) {
            log.error("Failed to send due-soon email", e);
        }

        log.info("Due soon reminder sent → user: {}", user.getEmail());
    }

    // ============================================
    //          NEW USER WELCOME
    // ============================================
    @Transactional
    public void sendWelcomeNotification(User user) {
        saveNotification(
            user,
            "Welcome to Library Management System!",
            "Thank you for registering. You can now browse and borrow books.",
            NotificationType.GENERAL
        );

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }

        log.info("Welcome notification sent → user: {}", user.getEmail());
    }

    // ============================================
    //          IN-APP NOTIFICATIONS
    // ============================================

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository
            .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setIsRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
        log.info("All notifications marked as read for user: {}", userId);
    }

    // ============================================
    //          PRIVATE HELPERS
    // ============================================

    private void saveNotification(User user, String title,
                                  String message, NotificationType type) {
        Notification notification = Notification.builder()
            .user(user)
            .title(title)
            .message(message)
            .type(type)
            .isRead(false)
            .sentVia("EMAIL,IN_APP")
            .build();
        notificationRepository.save(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .userId(notification.getUser() != null ? notification.getUser().getId() : null)
            .title(notification.getTitle())
            .message(notification.getMessage())
            .type(notification.getType())
            .isRead(notification.getIsRead())
            .sentVia(notification.getSentVia())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}