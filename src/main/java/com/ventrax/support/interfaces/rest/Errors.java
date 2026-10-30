package com.ventrax.support.interfaces.rest;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class Errors {
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    ProblemDetail validation(Exception e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Request validation failed");
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    ProblemDetail tooLarge(Exception e) {
        return ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    ProblemDetail malformed(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Malformed request");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalid(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({
        IllegalStateException.class,
        org.springframework.dao.DataIntegrityViolationException.class,
        org.springframework.dao.PessimisticLockingFailureException.class
    })
    ProblemDetail conflict(Exception e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Operation conflicts with current resource state");
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    ProblemDetail absent(Exception e) {
        return ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.client.RestClientException.class)
    ProblemDetail unavailable(Exception e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE, "Dependency unavailable");
    }
}
