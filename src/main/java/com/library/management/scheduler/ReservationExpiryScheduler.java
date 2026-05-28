package com.library.management.scheduler;

import com.library.management.entity.Reservation;
import com.library.management.enums.ReservationStatus;
import com.library.management.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryScheduler {

    private final ReservationRepository reservationRepository;

    // ============================================
    // RUNS EVERY HOUR
    // Auto-expires old reservations
    // ============================================
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expireOldReservations() {
        log.info("🔄 Checking for expired reservations...");

        List<Reservation> expired = reservationRepository
            .findExpiredReservations(LocalDateTime.now());

        for (Reservation reservation : expired) {
            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);
        }

        if (!expired.isEmpty()) {
            log.info("✅ Expired {} reservations", expired.size());
        }
    }
}