package com.library.management.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${library.email.from}")
    private String fromEmail;

    @Value("${library.email.from-name}")
    private String fromName;

    @Value("${library.email.enabled:true}")
    private boolean emailEnabled;

    // ============================================
    //          SEND PLAIN EMAIL
    // ============================================
    @Async
    public void sendEmail(String to, String subject, String body) {
        if (!emailEnabled) {
            log.info("📧 [DISABLED] Email to {}: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);
            log.info("✅ Email sent to: {}", to);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("❌ Failed to send email to: {}", to, e);
        }
    }

    // ============================================
    //          SEND HTML EMAIL (with template)
    // ============================================
    @Async
    public void sendHtmlEmail(String to, String subject,
                              String templateName,
                              Map<String, Object> variables) {

        if (!emailEnabled) {
            log.info("📧 [DISABLED] HTML Email to {}: {}", to, subject);
            return;
        }

        try {
            Context context = new Context();
            context.setVariables(variables);
            String htmlBody = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ HTML email sent to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send HTML email to: {}", to, e);
        }
    }

    // ============================================
    //          PRE-BUILT EMAIL TEMPLATES
    // ============================================

    public void sendWelcomeEmail(String to, String name) {
        Map<String, Object> vars = Map.of(
            "name", name,
            "loginUrl", "http://localhost:8080/login"
        );
        sendHtmlEmail(to, "Welcome to Library Management System!",
            "email/welcome", vars);
    }

    public void sendOverdueReminder(String to, String memberName,
                                     String bookTitle, long daysOverdue,
                                     String fineAmount) {
        Map<String, Object> vars = Map.of(
            "memberName", memberName,
            "bookTitle", bookTitle,
            "daysOverdue", daysOverdue,
            "fineAmount", fineAmount
        );
        sendHtmlEmail(to, "📚 Overdue Book Reminder",
            "email/overdue-reminder", vars);
    }

    public void sendDueSoonReminder(String to, String memberName,
                                     String bookTitle, String dueDate) {
        Map<String, Object> vars = Map.of(
            "memberName", memberName,
            "bookTitle", bookTitle,
            "dueDate", dueDate
        );
        sendHtmlEmail(to, "📖 Book Due Soon",
            "email/due-soon", vars);
    }

    public void sendReservationReadyEmail(String to, String memberName,
                                          String bookTitle, String expiryDate) {
        Map<String, Object> vars = Map.of(
            "memberName", memberName,
            "bookTitle", bookTitle,
            "expiryDate", expiryDate
        );
        sendHtmlEmail(to, "🎉 Your Reserved Book is Ready!",
            "email/reservation-ready", vars);
    }
}