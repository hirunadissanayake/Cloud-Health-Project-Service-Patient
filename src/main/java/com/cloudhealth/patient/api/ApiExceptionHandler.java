package com.cloudhealth.patient.api;

import com.cloudhealth.patient.service.ConflictException;
import com.cloudhealth.patient.service.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", exception.getMessage(), request);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    ResponseEntity<ProblemDetail> conflict(Exception exception, HttpServletRequest request) {
        var detail = exception instanceof ConflictException
                ? exception.getMessage()
                : "The request conflicts with existing patient data";
        return problem(HttpStatus.CONFLICT, "Data conflict", detail, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ProblemDetail> invalidArgument(IllegalArgumentException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> unreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                "The JSON body is malformed or contains an unsupported value",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "One or more fields are invalid");
        detail.setTitle("Validation failed");
        detail.setInstance(URI.create(request.getRequestURI()));

        var fieldErrors = new LinkedHashMap<String, String>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );
        detail.setProperty("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(detail);
    }

    private ResponseEntity<ProblemDetail> problem(
            HttpStatus status,
            String title,
            String detailMessage,
            HttpServletRequest request
    ) {
        var detail = ProblemDetail.forStatusAndDetail(status, detailMessage);
        detail.setTitle(title);
        detail.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(status).body(detail);
    }
}

