package com.coworking.api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador Global de Excepciones para la API REST.
 * Centraliza las respuestas de error estandarizadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================================================================
    // 1. ESTRUCTURA ESTÁNDAR DE RESPUESTA DE ERROR (RECORD JAVA 21)
    // =========================================================================
    public record ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        public static ApiErrorResponse of(HttpStatus status, String message, String path) {
            return new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, null);
        }

        public static ApiErrorResponse of(HttpStatus status, String message, String path, Map<String, String> validationErrors) {
            return new ApiErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, validationErrors);
        }
    }

    // =========================================================================
    // 2. EXCEPCIONES PERSONALIZADAS DE NEGOCIO
    // =========================================================================
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    // EXCEPCIÓN
    public static class OverlappingReservationException extends RuntimeException {
        public OverlappingReservationException(String message) {
            super(message);
        }
    }

    // =========================================================================
    // 3. CAPTURA DE EXCEPCIONES ESPECÍFICAS
    // =========================================================================

    // 409 - Solapamiento de Reserva (Conflicto de Horario)
    @ExceptionHandler(OverlappingReservationException.class)
    public ResponseEntity<ApiErrorResponse> handleOverlappingReservation(OverlappingReservationException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // 400 - Transiciones Inválidas del Patrón State (ej. Cancelar una reserva ya completada)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 404 - Recurso No Encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // 400 - Petición Incorrecta / Regla de Negocio
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 400 - Errores de Validación de Formularios/DTOs (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "Falló la validación de los datos enviados",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // 401 - Credenciales Inválidas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.UNAUTHORIZED, "Credenciales de acceso incorrectas", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 403 - Acceso Denegado / Permisos Insuficientes
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 500 - Error General No Controlado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        ApiErrorResponse response = ApiErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno en el servidor: " + ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

}
