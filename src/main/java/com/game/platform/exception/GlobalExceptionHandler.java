package com.game.platform.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ BUSINESS ERRORS → 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {
        return ResponseEntity
                .status(400)
                .body(Map.of(
                        "error", ex.getMessage(),
                        "status", 400
                ));
    }

    // ❌ REAL SERVER ERRORS → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception ex) {
        ex.printStackTrace(); // important

        return ResponseEntity
                .status(500)
                .body(Map.of(
                        "error", "Internal server error",
                        "status", 500
                ));
    }
}