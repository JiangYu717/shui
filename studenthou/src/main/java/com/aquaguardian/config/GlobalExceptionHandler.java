package com.aquaguardian.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, ex.getMessage()));
    }

    static class ErrorResponse {
        private int code;
        private String message;
        public ErrorResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }
        public int getCode() { return code; }
        public String getMessage() { return message; }
    }
} 