package com.coworking.api.domain.dto;

import com.coworking.api.domain.enums.ReservationStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(

        Long id,
        Long userId,
        String userName,
        Long spaceId,
        String spaceName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal totalPrice,
        ReservationStatusEnum status

) {}
