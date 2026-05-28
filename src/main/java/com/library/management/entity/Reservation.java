package com.library.management.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.library.management.enums.ReservationStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservations")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "reservation_date")
    private LocalDateTime reservationDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @Builder.Default
    private Boolean notified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    // Helper method to check if reservation is expired
    public boolean isExpired() {
        return status == ReservationStatus.EXPIRED || LocalDateTime.now().isAfter(expiryDate);
    }
}
