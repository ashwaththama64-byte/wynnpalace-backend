package com.game.platform.dto;

import java.math.BigDecimal;

public class DashboardResponse {

    private String name;
    private Long userId;
    private String userCode;

    // ✅ FIXED (BigDecimal)
    private BigDecimal balance;
    private BigDecimal betToday;
    private BigDecimal profit;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public BigDecimal getBetToday() {
        return betToday;
    }

    public void setBetToday(BigDecimal betToday) {
        this.betToday = betToday;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }
}