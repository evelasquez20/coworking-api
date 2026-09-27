package com.coworking.api.domain.dto;

import com.coworking.api.domain.enums.SpaceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record SpaceRequest(

        @NotBlank(message = "El nombre del espacio es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String name,

        @NotNull(message = "El tipo de espacio es obligatorio")
        SpaceType type,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad mínima debe ser 1 persona")
        Integer capacity,

        @NotBlank(message = "La ubicación es obligatoria")
        String location,

        @NotNull(message = "La tarifa por hora es obligatoria")
        @DecimalMin(value = "0.01", message = "La tarifa por hora debe ser mayor a 0")
        BigDecimal pricePerHour

) {}
