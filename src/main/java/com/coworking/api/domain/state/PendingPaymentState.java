package com.coworking.api.domain.state;

import com.coworking.api.domain.entity.Reservation;

public class PendingPaymentState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        reservation.setState(new ConfirmedState());
    }

    @Override
    public void cancel(Reservation reservation) {
        reservation.setState(new CancelledState());
    }

    @Override
    public String getStatusName() {
        return "PENDING_PAYMENT";
    }

}
