package com.library.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.library.management.enums.MembershipType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "membership_number", unique = true, nullable = false, length = 20)
    private String membershipNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_type", length = 20)
    @Builder.Default
    private MembershipType membershipType = MembershipType.STANDARD;

    @Column(name = "max_books_allowed")
    @Builder.Default
    private Integer maxBooksAllowed = 5;

    @Column(name = "max_borrow_days")
    @Builder.Default
    private Integer maxBorrowDays = 14;

    @Column(name = "membership_start", nullable = false)
    private LocalDate membershipStart;

    @Column(name = "membership_expiry", nullable = false)
    private LocalDate membershipExpiry;

    @Column(name = "total_fines_pending", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalFinesPending = BigDecimal.ZERO;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isMembershipExpired() {
        return LocalDate.now().isAfter(membershipExpiry);
    }
}