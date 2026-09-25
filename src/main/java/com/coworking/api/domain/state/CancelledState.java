package com.coworking.api.domain.state;

import com.coworking.api.domain.entity.Reservation;

public class CancelledState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        throw new IllegalStateException("No se puede confirmar una reserva que ha sido cancelada.");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new IllegalStateException("La reserva ya se encuentra cancelada.");
    }

    @Override
    public String getStatusName() {
        return "CANCELLED";
    }

}
