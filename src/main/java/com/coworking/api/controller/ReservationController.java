package com.coworking.api.controller;

import com.coworking.api.domain.dto.PaymentRequest;
import com.coworking.api.domain.dto.ReservationRequest;
import com.coworking.api.domain.dto.ReservationResponse;
import com.coworking.api.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request, authentication.getName()));
    }

    @GetMapping("/my-reservations")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(Authentication authentication) {
        return ResponseEntity.ok(reservationService.getUserReservations(authentication.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> cancelReservation(
            @PathVariable Long id,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(reservationService.cancelReservation(id, authentication.getName(), isAdmin));
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ReservationResponse> processPayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(reservationService.processPayment(id, request, authentication.getName()));
    }

}