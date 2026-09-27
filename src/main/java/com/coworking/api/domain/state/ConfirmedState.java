package com.coworking.api.domain.state;

import com.coworking.api.domain.entity.Reservation;

public class ConfirmedState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        throw new IllegalStateException("La reserva ya ha sido confirmada.");
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "CONFIRMED";
    }

}
