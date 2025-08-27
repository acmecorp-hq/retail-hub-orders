package com.acmecorp.retailhub.orders.api;

import com.acmecorp.retailhub.orders.api.dto.OrderDtos.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandling {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Problem> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getAllErrors().stream()
                .map(err -> {
                    if (err instanceof FieldError fe) {
                        return fe.getField() + ": " + fe.getDefaultMessage();
                    }
                    return err.getDefaultMessage();
                })
                .collect(Collectors.joining(", "));
        Problem problem = new Problem("Bad request", HttpStatus.BAD_REQUEST.value(), detail, "about:blank");
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Problem> handleNotFound(NotFoundException ex) {
        Problem problem = new Problem("Not found", HttpStatus.NOT_FOUND.value(), ex.getMessage(), "about:blank");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Problem> handleConflict(ConflictException ex) {
        Problem problem = new Problem("Conflict", HttpStatus.CONFLICT.value(), ex.getMessage(), "about:blank");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}


