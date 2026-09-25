package com.coworking.api.domain.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(

        @NotNull(message = "El ID del espacio es obligatorio")
        Long spaceId,

        @NotNull(message = "La fecha/hora de inicio es obligatoria")
        @Future(message = "La fecha de inicio debe ser en el futuro")
        LocalDateTime startTime,

        @NotNull(message = "La fecha/hora de fin es obligatoria")
        @Future(message = "La fecha de fin debe ser en el futuro")
        LocalDateTime endTime,

        @NotBlank(message = "El método de pago es obligatorio")
        String paymentMethodId

) {}
