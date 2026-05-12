package com.game.platform.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ✅ BUSINESS ERRORS → 400
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {

        Map<String, Object> res = new HashMap<>();

        res.put(
                "error",
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Something went wrong"
        );

        res.put("status", 400);

        return ResponseEntity
                .status(400)
                .body(res);
    }

    // ❌ REAL SERVER ERRORS → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handle(Exception ex) {

        ex.printStackTrace();

        Map<String, Object> res = new HashMap<>();

        res.put("error", "Internal server error");
        res.put("status", 500);

        return ResponseEntity
                .status(500)
                .body(res);
    }
}