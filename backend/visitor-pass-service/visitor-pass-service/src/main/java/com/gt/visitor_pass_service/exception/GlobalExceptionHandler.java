package com.gt.visitor_pass_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        System.err.println("=== VALIDATION EXCEPTION ===");
        System.err.println("Validation errors: " + ex.getBindingResult().getFieldErrors());

        Map<String, String> errors = new HashMap<>();
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> {
                    String fieldError = error.getField() + ": " + error.getDefaultMessage();
                    System.err.println("Field error: " + fieldError);
                    return fieldError;
                })
                .collect(Collectors.joining(", "));

        errors.put("message", errorMessage);
        System.err.println("Final error message: " + errorMessage);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        System.err.println("=== ILLEGAL ARGUMENT EXCEPTION ===");
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Request: " + request.getDescription(false));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

        // Check if this is an email uniqueness validation error
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email") &&
            ex.getMessage().toLowerCase().contains("already registered")) {
            errorResponse.put("errorType", "EMAIL_ALREADY_EXISTS");
            errorResponse.put("field", "email");
        } else {
            errorResponse.put("errorType", "VALIDATION_ERROR");
        }

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        System.err.println("=== RESOURCE NOT FOUND EXCEPTION ===");
        System.err.println("Message: " + ex.getMessage());
        System.err.println("Request: " + request.getDescription(false));

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getDescription(false).replace("uri=", ""));
        errorResponse.put("errorType", "RESOURCE_NOT_FOUND");

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
}