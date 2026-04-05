package com.game.platform.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fundPassword;

    // ✅ No-args constructor
    public RegisterRequest() {
    }

    // ✅ All-args constructor
    public RegisterRequest(String username, String password, String fundPassword) {
        this.username = username;
        this.password = password;
        this.fundPassword = fundPassword;
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

    public String getFundPassword() {
        return fundPassword;
    }

    public void setFundPassword(String fundPassword) {
        this.fundPassword = fundPassword;
    }
}