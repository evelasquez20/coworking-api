package com.coworking.api.service.impl;

import com.coworking.api.client.PaymentGatewayClient;
import com.coworking.api.domain.dto.PaymentRequest;
import com.coworking.api.domain.dto.PaymentResponse;
import com.coworking.api.domain.dto.ReservationRequest;
import com.coworking.api.domain.dto.ReservationResponse;
import com.coworking.api.domain.entity.Reservation;
import com.coworking.api.domain.entity.Space;
import com.coworking.api.domain.entity.User;
import com.coworking.api.domain.enums.ReservationStatusEnum;
import com.coworking.api.domain.state.ReservationState;
import com.coworking.api.event.ReservationConfirmedEvent;
import com.coworking.api.exception.BusinessException;
import com.coworking.api.exception.ErrorCode;
import com.coworking.api.repository.ReservationRepository;
import com.coworking.api.repository.SpaceRepository;
import com.coworking.api.repository.UserRepository;
import com.coworking.api.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayClient paymentGatewayClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request, String userEmail) {
        log.info("Iniciando creación de reserva para el usuario: '{}' en el espacio ID: {}", userEmail, request.spaceId());

        if (request.endTime().isBefore(request.startTime()) || request.endTime().isEqual(request.startTime())) {
            log.warn("Error al crear reserva: Rango de tiempo inválido. Inicio: {}, Fin: {}", request.startTime(), request.endTime());
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Error al crear reserva: Usuario no encontrado con email: '{}'", userEmail);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        Space space = spaceRepository.findById(request.spaceId())
                .orElseThrow(() -> {
                    log.warn("Error al crear reserva: Espacio no encontrado con ID: {}", request.spaceId());
                    return new BusinessException(ErrorCode.SPACE_NOT_FOUND);
                });

        // Validación de solapamiento de horario con Bloqueo Pesimista
        List<ReservationStatusEnum> activeStatuses = List.of(
                ReservationStatusEnum.PENDING_PAYMENT,
                ReservationStatusEnum.CONFIRMED
        );

        boolean isOverlapping = reservationRepository.existsOverlappingReservation(
                space.getId(),
                request.startTime(),
                request.endTime(),
                activeStatuses
        );

        if (isOverlapping) {
            log.warn("Error al crear reserva: Solapamiento detectado para el espacio ID: {} entre {} y {}",
                    space.getId(), request.startTime(), request.endTime());
            throw new BusinessException(ErrorCode.RESERVATION_OVERLAP);
        }

        // Cálculo de costo total
        long hours = Duration.between(request.startTime(), request.endTime()).toHours();
        if (hours == 0) hours = 1; // Mínimo 1 hora
        BigDecimal totalCost = space.getHourlyRate().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = Reservation.builder()
                .user(user)
                .space(space)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .totalCost(totalCost)
                .status(ReservationStatusEnum.PENDING_PAYMENT)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Creación de reserva completada exitosamente con ID: {} en estado PENDING_PAYMENT para el usuario: '{}'",
                savedReservation.getId(), userEmail);

        return mapToResponse(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getUserReservations(String userEmail) {
        log.info("Iniciando consulta de reservas para el usuario: '{}'", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Consulta fallida: Usuario no encontrado con email: '{}'", userEmail);
                    return new BusinessException(ErrorCode.USER_NOT_FOUND);
                });

        List<ReservationResponse> reservations = reservationRepository.findByUserId(user.getId()).stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Consulta de reservas completada exitosamente para el usuario: '{}'. Total recuperado: {}", userEmail, reservations.size());
        return reservations;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        log.info("Iniciando consulta global de todas las reservas (Modo Admin)");

        List<ReservationResponse> reservations = reservationRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();

        log.info("Consulta global de reservas completada exitosamente. Total recuperado: {} reservas", reservations.size());
        return reservations;
    }

    @Override
    @Transactional
    public ReservationResponse cancelReservation(Long reservationId, String userEmail, boolean isAdmin) {
        log.info("Iniciando proceso de cancelación para la reserva ID: {} solicitada por el usuario: '{}' (Es Admin: {})",
                reservationId, userEmail, isAdmin);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.warn("Error al cancelar: No se encontró la reserva con ID: {}", reservationId);
                    return new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
                });

        if (!isAdmin && !reservation.getUser().getEmail().equals(userEmail)) {
            log.warn("Acceso denegado: El usuario '{}' intentó cancelar la reserva ID: {} que pertenece a otro usuario",
                    userEmail, reservationId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }

        // Aplicación del patrón State para la transición de estado
        ReservationState currentState = reservation.getState();
        currentState.cancel(reservation);

        Reservation updatedReservation = reservationRepository.save(reservation);
        log.info("Cancelación de la reserva ID: {} completada exitosamente. Nuevo estado: {}",
                updatedReservation.getId(), updatedReservation.getStatus());

        return mapToResponse(updatedReservation);
    }

    @Override
    @Transactional
    public ReservationResponse processPayment(Long reservationId, PaymentRequest request, String userEmail) {
        log.info("Iniciando proceso de pago para la reserva ID: {} solicitado por el usuario: '{}'", reservationId, userEmail);

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> {
                    log.warn("Proceso de pago fallido: No se encontró la reserva con ID: {}", reservationId);
                    return new BusinessException(ErrorCode.RESERVATION_NOT_FOUND);
                });

        if (!reservation.getUser().getEmail().equals(userEmail)) {
            log.warn("Acceso denegado: El usuario '{}' no es propietario de la reserva ID: {}", userEmail, reservationId);
            throw new BusinessException(ErrorCode.UNAUTHORIZED_RESERVATION_ACCESS);
        }

        PaymentResponse paymentResult = paymentGatewayClient.processPayment(
                reservation.getId(),
                reservation.getTotalCost(),
                request.paymentMethodId()
        );

        if (paymentResult.successful()) {
            reservation.confirm();
            log.info("Estado de la reserva ID: {} actualizado a CONFIRMED", reservation.getId());

            // Publicación del evento asíncrono de confirmación
            eventPublisher.publishEvent(new ReservationConfirmedEvent(
                    reservation.getId(),
                    reservation.getUser().getEmail(),
                    reservation.getSpace().getName(),
                    reservation.getTotalCost(),
                    reservation.getStartTime(),
                    reservation.getEndTime()
            ));
            log.info("[EVENT-PUBLISHED] Evento ReservationConfirmedEvent emitido para la reserva ID: {}", reservation.getId());
        } else {
            log.warn("El pago no fue completado para la reserva ID: {}. Razón: {}", reservation.getId(), paymentResult.message());
        }

        Reservation savedReservation = reservationRepository.save(reservation);

        log.info("Proceso de pago finalizado con éxito para la reserva ID: {}", savedReservation.getId());
        return mapToResponse(savedReservation);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getName(),
                reservation.getUser().getEmail(),
                reservation.getSpace().getId(),
                reservation.getSpace().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getTotalCost(),
                reservation.getStatus()
        );
    }

}
