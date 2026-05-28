package com.library.management.scheduler;

import com.library.management.entity.Member;
import com.library.management.repository.MemberRepository;
import com.library.management.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MembershipExpiryScheduler {

    private final MemberRepository memberRepository;
    private final EmailService emailService;

    // ============================================
    // RUNS EVERY DAY AT 8:00 AM
    // Notifies members whose membership expires soon
    // ============================================
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyMembershipExpiring() {
        log.info("📧 Checking memberships expiring in 7 days...");

        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        List<Member> expiringSoon = memberRepository
            .findExpiredMemberships(sevenDaysFromNow);

        for (Member member : expiringSoon) {
            try {
                emailService.sendEmail(
                    member.getUser().getEmail(),
                    "Your Membership is Expiring Soon",
                    String.format(
                        "Dear %s,\n\n" +
                        "Your library membership expires on %s.\n" +
                        "Please renew before expiry to continue borrowing books.\n\n" +
                        "Thank you,\nLibrary Management System",
                        member.getUser().getFullName(),
                        member.getMembershipExpiry()
                    )
                );
            } catch (Exception e) {
                log.error("Failed to send membership expiry email", e);
            }
        }

        log.info("✅ Notified {} members about expiring memberships",
            expiringSoon.size());
    }
}