package com.coworking.api.domain.dto;

import com.coworking.api.domain.enums.SpaceType;
import java.math.BigDecimal;

public record SpaceResponse(

        Long id,
        String name,
        SpaceType type,
        Integer capacity,
        String location,
        BigDecimal pricePerHour

) {}
