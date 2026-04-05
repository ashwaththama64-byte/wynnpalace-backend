package com.game.platform.dto;

public class AuthResponse {

    private String token;
    private String refreshToken;
    private String role;

    // ✅ UPDATED CONSTRUCTOR
    public AuthResponse(String token, String refreshToken, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getRole() {
        return role;
    }
}