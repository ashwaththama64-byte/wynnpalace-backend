package com.game.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "Username required")
    private String username;

    @NotBlank(message = "Password required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Fund password required")
    @Size(min = 4, message = "Fund password must be at least 4 characters")
    private String fundPassword;

    public RegisterRequest() {}

    public RegisterRequest(String username, String password, String fundPassword) {
        this.username = username;
        this.password = password;
        this.fundPassword = fundPassword;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFundPassword() { return fundPassword; }
    public void setFundPassword(String fundPassword) { this.fundPassword = fundPassword; }
}