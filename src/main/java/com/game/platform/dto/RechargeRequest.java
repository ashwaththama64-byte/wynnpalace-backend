package com.game.platform.dto;

import java.math.BigDecimal;

public class RechargeRequest {

    private Long userId;

    // ✅ FIXED
    private BigDecimal amount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}