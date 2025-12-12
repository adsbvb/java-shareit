package ru.practicum.shareit.error;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final MethodArgumentNotValidException e) {
        log.error("Validation error: " + e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError
                    ? ((FieldError) error).getField()
                    : error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Validation failed for " + errors.size() + " field(s)";

        ErrorResponse errorResponse = new ErrorResponse(
                "Bad Request",
                message,
                HttpStatus.BAD_REQUEST.value()
        );

        errorResponse.setDetails(errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final ConstraintViolationException e) {
        log.error("Constraint validation error: " + e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getConstraintViolations().forEach((error) -> {
            String fieldName = error.getPropertyPath().toString();
            String errorMessage = error.getMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Validation failed for " + errors.size() + " constraint(s)";

        ErrorResponse errorResponse = new ErrorResponse(
                "Bad Request",
                message,
                HttpStatus.BAD_REQUEST.value()
        );

        errorResponse.setDetails(errors);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(final IllegalArgumentException e) {
        log.error("IllegalArgumentException: " + e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "Bad Request",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error("Exception: " + e.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "Bad Request",
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

