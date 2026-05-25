package com.adarsh.financemanager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 409 Conflict ──────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleResourceAlreadyExists(
            ResourceAlreadyExistsException ex
    ) {
        return buildError(ex.getMessage(), HttpStatus.CONFLICT);
    }

    // ── 404 Not Found ─────────────────────────────────────────────────────────
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(
            ResourceNotFoundException ex
    ) {
        return buildError(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // ── 404 Unknown URL ───────────────────────────────────────────────────────
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFound(
            NoResourceFoundException ex
    ) {
        return buildError("The requested endpoint does not exist", HttpStatus.NOT_FOUND);
    }

    // ── 403 Forbidden (domain ownership) ──────────────────────────────────────
    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenAccess(
            ForbiddenAccessException ex
    ) {
        return buildError(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // ── 400 Category in use ───────────────────────────────────────────────────
    @ExceptionHandler(CategoryInUseException.class)
    public ResponseEntity<Map<String, String>> handleCategoryInUse(
            CategoryInUseException ex
    ) {
        return buildError(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ── 400 Validation ────────────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // ── 400 Malformed JSON / bad enum value ───────────────────────────────────
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {
        String message = "Malformed or unreadable request body";
        if (ex.getMessage() != null && ex.getMessage().contains("not one of the values accepted")) {
            message = "Invalid value provided. Check enum fields (e.g. type must be INCOME or EXPENSE)";
        }
        return buildError(message, HttpStatus.BAD_REQUEST);
    }

    // ── 400 Bad argument (invalid enum, type mismatch) ────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(
            IllegalArgumentException ex
    ) {
        return buildError("Invalid request parameter: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ── 400 Illegal State (generic business rule violations) ──────────────────
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(
            IllegalStateException ex
    ) {
        return buildError(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ── 401 Unauthorized ──────────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            BadCredentialsException ex
    ) {
        return buildError("Invalid username or password", HttpStatus.UNAUTHORIZED);
    }

    // ── 400 Constraint Violation ──────────────────────────────────────────────
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            jakarta.validation.ConstraintViolationException ex
    ) {
        return buildError(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ── 400 Method Argument Type Mismatch ──────────────────────────────────────
    @ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex
    ) {
        String message = "Invalid parameter value for: " + ex.getName();
        return buildError(message, HttpStatus.BAD_REQUEST);
    }

    // ── 401 Authentication Failure ─────────────────────────────────────────────
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex
    ) {
        return buildError(ex.getMessage() != null ? ex.getMessage() : "Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    // ── 500 Generic fallback ──────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(
            Exception ex
    ) {
        return buildError("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }


    // ── Helper ────────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, String>> buildError(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return new ResponseEntity<>(error, status);
    }
}