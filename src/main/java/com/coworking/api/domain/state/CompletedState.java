package com.coworking.api.domain.state;

import com.coworking.api.domain.entity.Reservation;

public class CompletedState implements ReservationState {

    @Override
    public void confirm(Reservation reservation) {
        throw new IllegalStateException("La reserva ya finalizó y no puede modificarse.");
    }

    @Override
    public void cancel(Reservation reservation) {
        throw new IllegalStateException("No se puede cancelar una reserva que ya fue completada.");
    }

    @Override
    public String getStatusName() {
        return "COMPLETED";
    }

}
