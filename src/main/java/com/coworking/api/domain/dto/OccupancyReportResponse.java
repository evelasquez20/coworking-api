package com.coworking.api.domain.dto;

import java.math.BigDecimal;

public record OccupancyReportResponse(

        Long spaceId,
        String spaceName,
        Long totalReservedHours,
        BigDecimal occupancyPercentage

) {}
