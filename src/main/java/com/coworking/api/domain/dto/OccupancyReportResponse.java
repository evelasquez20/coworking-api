package com.coworking.api.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OccupancyReportResponse(

        LocalDate startDate,
        LocalDate endDate,
        Long totalReservations,
        BigDecimal occupancyPercentage,
        Long totalReservedHours
) {}
