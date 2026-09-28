package com.coworking.api.domain.dto;

import com.coworking.api.domain.enums.SpaceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SpaceRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotNull(message = "El tipo de espacio es obligatorio")
        SpaceType type,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser de al menos 1 persona")
        Integer capacity,

        @NotBlank(message = "La ubicación es obligatoria")
        String location,

        @NotNull(message = "La tarifa por hora es obligatoria")
        @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa por hora debe ser mayor a 0")
        BigDecimal hourlyRate

) {}