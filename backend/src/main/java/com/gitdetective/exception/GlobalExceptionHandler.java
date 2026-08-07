package com.gitdetective.exception;

import com.gitdetective.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Centralized exception-to-response mapping.
 *
 * <p>Ensures every failure returns a structured {@link ErrorResponse} with logging context.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException exception, HttpServletRequest request) {
        log.warn(
                "API exception [{}] on {}: {}",
                exception.getErrorCode(),
                request.getRequestURI(),
                exception.getMessage());

        return ResponseEntity.status(exception.getStatus())
                .body(
                        buildError(
                                exception.getStatus(),
                                exception.getErrorCode(),
                                exception.getMessage(),
                                request.getRequestURI(),
                                null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<ErrorResponse.FieldViolation> violations =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(this::toViolation)
                        .toList();

        log.warn(
                "Validation failed on {} with {} violation(s)",
                request.getRequestURI(),
                violations.size());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(
                        buildError(
                                HttpStatus.BAD_REQUEST,
                                "VALIDATION_FAILED",
                                "Request validation failed",
                                request.getRequestURI(),
                                violations));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(
                        buildError(
                                HttpStatus.NOT_FOUND,
                                "ENDPOINT_NOT_FOUND",
                                "The requested endpoint was not found",
                                request.getRequestURI(),
                                null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        log.error(
                "Unexpected error on {}: {}",
                request.getRequestURI(),
                exception.getMessage(),
                exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        buildError(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "INTERNAL_SERVER_ERROR",
                                "An unexpected error occurred",
                                request.getRequestURI(),
                                null));
    }

    private ErrorResponse.FieldViolation toViolation(FieldError fieldError) {
        return ErrorResponse.FieldViolation.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private ErrorResponse buildError(
            HttpStatus status,
            String errorCode,
            String message,
            String path,
            List<ErrorResponse.FieldViolation> violations) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .path(path)
                .timestamp(Instant.now())
                .violations(violations)
                .build();
    }
}
