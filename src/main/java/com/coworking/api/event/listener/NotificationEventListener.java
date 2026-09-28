package com.coworking.api.event.listener;

import com.coworking.api.event.ReservationConfirmedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    @Async
    @EventListener
    public void handleReservationConfirmed(ReservationConfirmedEvent event) {
        log.info("[ASYNC-EVENT] Evento de confirmación recibido para la reserva ID: {}. Iniciando proceso de envío de correo", event.reservationId());

        try {
            // Simulación del envío de correo (delay de red de 2 segundos)
            Thread.sleep(2000);
            log.info("[ASYNC-EVENT] Correo de confirmación enviado exitosamente al usuario: '{}' para el espacio: '{}' (Monto: ${})",
                    event.userEmail(), event.spaceName(), event.totalCost());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[ASYNC-EVENT] Error al procesar la notificación de correo para la reserva ID: {}", event.reservationId(), e);
        }
    }

}
