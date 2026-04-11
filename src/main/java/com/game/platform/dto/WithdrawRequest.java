package com.game.platform.dto;

import java.math.BigDecimal;

public class WithdrawRequest {

    private BigDecimal amount; // ✅ FIXED
    private String fundPassword;
    private Long cardId;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getFundPassword() {
        return fundPassword;
    }

    public void setFundPassword(String fundPassword) {
        this.fundPassword = fundPassword;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }
}