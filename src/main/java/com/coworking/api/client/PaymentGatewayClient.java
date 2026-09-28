package com.coworking.api.client;

import com.coworking.api.config.security.AppProperties;
import com.coworking.api.domain.dto.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGatewayClient {

    private final AppProperties appProperties;
    private final RestClient restClient = RestClient.create();

    @CircuitBreaker(name = "paymentService", fallbackMethod = "processPaymentFallback")
    public PaymentResponse processPayment(Long reservationId, BigDecimal amount, String paymentMethodId) {
        log.info("Iniciando llamada externa a pasarela de pagos para la reserva ID: {} por un monto de: {}", reservationId, amount);

        Map<String, Object> requestBody = Map.of(
                "reservationId", reservationId,
                "amount", amount,
                "paymentMethodId", paymentMethodId
        );

        PaymentResponse response = restClient.post()
                .uri(appProperties.getPaymentGateway().getUrl())
                .body(requestBody)
                .retrieve()
                .body(PaymentResponse.class);

        log.info("Llamada externa a pasarela de pagos finalizada exitosamente para la reserva ID: {}. Transaction ID: {}",
                reservationId, response != null ? response.transactionId() : "N/A");

        return response;
    }

    public PaymentResponse processPaymentFallback(Long reservationId, BigDecimal amount, String paymentMethodId, Throwable throwable) {
        // Log de advertencia/fallback cuando falla el servicio externo o el circuito está ABIERTO
        log.warn("Fallback activado en PaymentGatewayClient para la reserva ID: {}. Motivo: {}",
                reservationId, throwable.getMessage());

        return new PaymentResponse(
                false,
                null,
                "Pago diferido: El servicio de pagos no está disponible temporalmente. La reserva permanece en PENDING_PAYMENT."
        );
    }

}