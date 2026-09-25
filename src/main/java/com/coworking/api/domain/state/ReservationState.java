package com.coworking.api.domain.state;

import com.coworking.api.domain.entity.Reservation;

public interface ReservationState {

    void confirm(Reservation reservation);
    void cancel(Reservation reservation);
    String getStatusName();

}
