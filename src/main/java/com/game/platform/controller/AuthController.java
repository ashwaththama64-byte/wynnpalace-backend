package com.game.platform.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.game.platform.dto.AuthResponse;
import com.game.platform.dto.LoginRequest;
import com.game.platform.dto.RegisterRequest;
import com.game.platform.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    private final AuthService service;

    // ✅ Constructor Injection (no Lombok)
    public AuthController(AuthService service) {
        this.service = service;
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        service.register(req);
        return ResponseEntity.ok("Registered Successfully");
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }

    // ✅ REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(service.refreshToken(refreshToken));
    }

    // ✅ VERIFY FUND PASSWORD
    @PostMapping("/verify-fund-password")
    public ResponseEntity<?> verifyFundPassword(
            @RequestParam String fundPassword,
            Principal principal) {

        service.verifyFundPassword(principal.getName(), fundPassword);
        return ResponseEntity.ok("Verified");
    }
}