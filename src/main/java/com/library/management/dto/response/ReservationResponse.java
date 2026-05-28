package com.library.management.dto.response;

import com.library.management.enums.ReservationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private String membershipNumber;
    private Long bookId;
    private String bookTitle;
    private String isbn;
    private LocalDateTime reservationDate;
    private LocalDateTime expiryDate;
    private ReservationStatus status;
    private Boolean notified;
    private LocalDateTime createdAt;
}