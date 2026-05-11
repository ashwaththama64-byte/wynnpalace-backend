package com.game.platform.dto;

public class AuthResponse {

    private String token;

    private String role;
    private Long userId;

    // ✅ UPDATED CONSTRUCTOR
    public AuthResponse(String token,String role, Long userId) {
        this.token = token;
 
        this.role = role;
        this.userId=userId;
    }
public AuthResponse() {
	// TODO Auto-generated constructor stub
}

	public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }

	public Long getUserId() {
		return userId;
	}
    
}