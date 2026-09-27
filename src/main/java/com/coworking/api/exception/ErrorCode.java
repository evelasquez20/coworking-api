package com.coworking.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Errores del Framework / Sistema
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VAL_001", "Falló la validación de los datos enviados"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_403", "No tienes permisos para realizar esta acción"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_500", "Ha ocurrido un error interno en el servidor"),

    // Errores de Autenticación y Usuarios
    EMAIL_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "AUTH_001", "El correo electrónico ya se encuentra registrado"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "Credenciales de acceso inválidas"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_003", "El usuario solicitado no existe"),

    // Errores de Espacios
    SPACE_NOT_FOUND(HttpStatus.NOT_FOUND, "SPACE_001", "El espacio de coworking solicitado no existe"),
    SPACE_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "SPACE_002", "Ya existe un espacio registrado con ese nombre"),

    // Errores de Reservas
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "RES_001", "La reserva solicitada no existe"),
    RESERVATION_OVERLAP(HttpStatus.CONFLICT, "RES_002", "El espacio ya cuenta con una reserva confirmada en el horario seleccionado"),
    INVALID_RESERVATION_STATE(HttpStatus.BAD_REQUEST, "RES_003", "Transición de estado no permitida para la reserva"),

    // Integraciones Externas / Pagos
    PAYMENT_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "EXT_001", "El servicio de pagos no está disponible temporalmente");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}