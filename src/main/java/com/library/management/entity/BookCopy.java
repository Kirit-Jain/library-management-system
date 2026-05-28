package com.library.management.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.library.management.enums.BookCondition;
import com.library.management.enums.BookCopyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_copies")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(unique = true, nullable = false, length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelf_id")
    private Shelf shelf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private LibraryBranch branch;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private BookCondition condition = BookCondition.GOOD;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private BookCopyStatus status = BookCopyStatus.AVAILABLE;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}