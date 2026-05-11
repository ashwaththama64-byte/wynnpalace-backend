package com.game.platform.dto;

import java.math.BigDecimal;

public class UserResponse {

    private Long id;
    private String username;
    private String userCode;

    // ✅ FIXED (BigDecimal)
    private BigDecimal balance;
    
    private BigDecimal withdrawable; 

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public BigDecimal getWithdrawable() {
		return withdrawable;
	}

	public void setWithdrawable(BigDecimal withdrawable) {
		this.withdrawable = withdrawable;
	}

	public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}