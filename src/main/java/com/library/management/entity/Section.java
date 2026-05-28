package com.library.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sections")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 10)
    private String code;
}