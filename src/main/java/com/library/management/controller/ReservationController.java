package com.library.management.controller;

import com.library.management.dto.request.ReservationRequest;
import com.library.management.dto.response.ApiResponse;
import com.library.management.dto.response.ReservationResponse;
import com.library.management.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Book reservation management")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @Operation(summary = "Reserve a book")
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
        @Valid @RequestBody ReservationRequest request) {

        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Book reserved successfully", response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel a reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancelReservation(
        @PathVariable Long id,
        @RequestParam Long memberId) {

        ReservationResponse response = reservationService.cancelReservation(id, memberId);
        return ResponseEntity.ok(ApiResponse.success("Reservation cancelled", response));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get all reservations for a member")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getMemberReservations(
        @PathVariable Long memberId) {

        return ResponseEntity.ok(
            ApiResponse.success("Member reservations fetched successfully", reservationService.getMemberReservations(memberId)));
    }
}