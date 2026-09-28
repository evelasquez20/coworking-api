package com.coworking.api.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationConfirmedEvent (

    Long reservationId,
    String userEmail,
    String spaceName,
    BigDecimal totalCost,
    LocalDateTime startTime,
    LocalDateTime endTime

 ) {}
