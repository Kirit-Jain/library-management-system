package com.library.management.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.library.management.enums.FineStatus;
import com.library.management.enums.FineType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fines")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrowing_id", nullable = false)
    private Borrowing borrowing;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "fine_type", nullable = false, length = 30)
    private FineType fineType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private FineStatus status = FineStatus.UNPAID;

    @Column(name = "fine_date", nullable = false)
    private LocalDate fineDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "paid_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;


    // Helper method to calculate remaining amount
    public BigDecimal getRemainingAmount() {
        return amount.subtract(paidAmount);
    }
}
