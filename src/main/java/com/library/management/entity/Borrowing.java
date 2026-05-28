package com.library.management.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.library.management.enums.BorrowingStatus;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "borrowings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @Column(name = "borrow_date")
    private LocalDateTime borrowDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Column(name = "renewed_count")
    @Builder.Default
    private Integer renewedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private BorrowingStatus status = BorrowingStatus.ACTIVE;

    @Column(name = "issued_by")
    private Long issuedBy; // issuer's ID

    @Column(name = "returned_to")
    private Long returnedTo; // returner's ID

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper Functions to check the overdue status and calculate overdue days

    public boolean isOverdue() {
        if (this.status == BorrowingStatus.RETURNED) return false;
        return LocalDateTime.now().isAfter(this.dueDate);
    }

    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        return ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
    }
}
