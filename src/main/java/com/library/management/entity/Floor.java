package com.library.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "floors")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private LibraryBranch branch;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(length = 50)
    private String name;
}