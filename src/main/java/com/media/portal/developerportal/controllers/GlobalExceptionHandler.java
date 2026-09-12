package com.media.portal.developerportal.controllers;

import com.media.portal.developerportal.model.IllegalStateTransitionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problemDetail.setTitle("Resource Not Found");
        problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(BadApiRequestException.class)
    public ProblemDetail handleBadApiRequestException(BadApiRequestException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problemDetail.setTitle("Bad request");
        problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String detail = "A database constraint violation occurred.";

        Throwable rootCause = ex.getRootCause();
        String message = rootCause != null ? rootCause.getMessage() : ex.getMessage();

        if (message != null && message.contains("violates unique constraint")) {
            detail = "Duplicate entry violation: " + message;
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
        problemDetail.setTitle("Database Conflict / Duplicate Entry");
        problemDetail.setType(URI.create("https://api.example.com/errors/database-conflict"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(IllegalStateTransitionException.class)
    public ProblemDetail handleIllegalStateTransitionException(IllegalStateTransitionException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problemDetail.setTitle("Bad request");
        problemDetail.setType(URI.create("https://api.example.com/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

}
