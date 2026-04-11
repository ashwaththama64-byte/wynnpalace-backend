package com.game.platform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.game.platform.dto.LoginRequest;
import com.game.platform.service.AdminService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin("*")
public class AdminAuthController {

    private final AdminService adminService;

    public AdminAuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        System.out.println("🔥 ADMIN LOGIN START");

        String token = adminService.login(
                request.getUsername(),
                request.getPassword()
        );

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        System.out.println("🚀 ADMIN TOKEN GENERATED");

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "role", "ADMIN"
                )
        );
    }
}