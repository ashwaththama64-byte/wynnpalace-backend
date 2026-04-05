package com.game.platform.dto;

public class LoginRequest {

    private String username;
    private String password;

    // ✅ No-args constructor
    public LoginRequest() {
    }

    // ✅ All-args constructor (optional but good)
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ✅ Getters & Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}