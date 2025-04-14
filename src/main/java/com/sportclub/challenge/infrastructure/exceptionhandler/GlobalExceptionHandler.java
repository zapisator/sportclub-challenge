package com.sportclub.challenge.infrastructure.exceptionhandler;

import com.sportclub.challenge.adapter.in.web.dto.response.ErrorResponseDto;
import com.sportclub.challenge.application.exception.AccountStatusException;
import com.sportclub.challenge.application.exception.AuthenticationFailedException;
import com.sportclub.challenge.application.exception.BranchNotFoundException;
import com.sportclub.challenge.application.exception.InvalidDniFormatException;
import com.sportclub.challenge.application.exception.MigrationFailedException;
import com.sportclub.challenge.application.exception.SportClubAccessDeniedException;
import com.sportclub.challenge.application.exception.UserNotFoundException;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final LoggingPort log;

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(BranchNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleBranchNotFound(BranchNotFoundException ex, HttpServletRequest request) {
        log.warn("Branch not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Branch Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(SportClubAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleSportClubAccessDenied(SportClubAccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidDniFormatException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDniFormat(InvalidDniFormatException ex, HttpServletRequest request) {
        log.warn("Invalid DNI format: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid Input", ex.getMessage(), request);
    }

    @ExceptionHandler(MigrationFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleMigrationFailed(MigrationFailedException ex, HttpServletRequest request) {
        log.error("Migration failed: {}", ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Migration Error", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationFailed(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        String message = "Validation failed: " + errors;
        log.warn(message);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", message, request);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(AuthenticationFailedException ex, HttpServletRequest request) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "Invalid credentials provided.", request);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleAccountStatus(AccountStatusException ex, HttpServletRequest request) {
        log.warn("Authentication failed due to account status: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", "User account status issue: " + ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleSpringAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Authorization failed: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Forbidden", "You do not have permission to access this resource.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        final String message = "An internal server error occurred. Please try again later.";
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", message, request);
    }


    private ResponseEntity<ErrorResponseDto> buildErrorResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        ErrorResponseDto errorDto = new ErrorResponseDto(
                Instant.now().toEpochMilli(),
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDto, status);
    }
}
