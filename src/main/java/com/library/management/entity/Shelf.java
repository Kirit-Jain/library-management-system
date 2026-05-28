package com.library.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shelves")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shelf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "shelf_code", unique = true, nullable = false, length = 20)
    private String shelfCode;

    @Builder.Default
    private Integer capacity = 100;
}