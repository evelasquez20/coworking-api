package com.coworking.api.domain.dto;

public record PaymentResponse(

        boolean successful,
        String transactionId,
        String message

) {}