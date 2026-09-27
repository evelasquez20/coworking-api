package com.coworking.api.exception;

import jakarta.servlet.http.HttpServletRequest;
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

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ApiErrorResponse(
            LocalDateTime timestamp,
            int status,
            String errorCode,
            String error,
            String message,
            String path,
            Map<String, String> validationErrors
    ) {
        public static ApiErrorResponse of(ErrorCode errorCode, String message, String path) {
            return new ApiErrorResponse(
                    LocalDateTime.now(),
                    errorCode.getHttpStatus().value(),
                    errorCode.getCode(),
                    errorCode.getHttpStatus().getReasonPhrase(),
                    message,
                    path,
                    null
            );
        }

        public static ApiErrorResponse of(ErrorCode errorCode, String message, String path, Map<String, String> validationErrors) {
            return new ApiErrorResponse(
                    LocalDateTime.now(),
                    errorCode.getHttpStatus().value(),
                    errorCode.getCode(),
                    errorCode.getHttpStatus().getReasonPhrase(),
                    message,
                    path,
                    validationErrors
            );
        }
    }

    // 1. Excepciones de negocio propias
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ErrorCode ec = ex.getErrorCode();
        ApiErrorResponse response = ApiErrorResponse.of(ec, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ec.getHttpStatus()).body(response);
    }

    // 2. Validaciones de DTOs (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorCode ec = ErrorCode.VALIDATION_ERROR;
        ApiErrorResponse response = ApiErrorResponse.of(ec, ec.getDefaultMessage(), request.getRequestURI(), errors);
        return ResponseEntity.status(ec.getHttpStatus()).body(response);
    }

    // 3. Credenciales incorrectas
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        ErrorCode ec = ErrorCode.INVALID_CREDENTIALS;
        ApiErrorResponse response = ApiErrorResponse.of(ec, ec.getDefaultMessage(), request.getRequestURI());
        return ResponseEntity.status(ec.getHttpStatus()).body(response);
    }

    // 4. Acceso denegado (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ErrorCode ec = ErrorCode.ACCESS_DENIED;
        ApiErrorResponse response = ApiErrorResponse.of(ec, ec.getDefaultMessage(), request.getRequestURI());
        return ResponseEntity.status(ec.getHttpStatus()).body(response);
    }

    // 5. Errores no controlados (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        ErrorCode ec = ErrorCode.INTERNAL_SERVER_ERROR;
        ApiErrorResponse response = ApiErrorResponse.of(ec, ec.getDefaultMessage() + ": " + ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ec.getHttpStatus()).body(response);
    }

}