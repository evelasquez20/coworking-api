package com.coworking.api.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentRequest(

        @NotBlank(message = "El método de pago es obligatorio")
        String paymentMethodId

) {}