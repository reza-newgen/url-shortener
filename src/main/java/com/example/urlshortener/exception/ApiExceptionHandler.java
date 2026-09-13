package com.example.urlshortener.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(UrlNotFoundException e) {
        ProblemDetail p = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        p.setDetail(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(p);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> badRequest(IllegalArgumentException e) {
        ProblemDetail p = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        p.setDetail(e.getMessage());
        return ResponseEntity.badRequest().body(p);
    }
}