package com.coworking.api.service;

import com.coworking.api.domain.dto.PaymentRequest;
import com.coworking.api.domain.dto.ReservationRequest;
import com.coworking.api.domain.dto.ReservationResponse;

import java.util.List;

public interface ReservationService {

    ReservationResponse createReservation(ReservationRequest request, String userEmail);
    List<ReservationResponse> getUserReservations(String userEmail);
    List<ReservationResponse> getAllReservations();
    ReservationResponse cancelReservation(Long reservationId, String userEmail, boolean isAdmin);
    ReservationResponse processPayment(Long reservationId, PaymentRequest request, String userEmail);

}
