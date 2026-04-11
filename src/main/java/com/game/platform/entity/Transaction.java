package com.game.platform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String type; // RECHARGE, WITHDRAW, BET, REWARD

    // ✅ MONEY FIELD (SAFE)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String status; // SUCCESS, PENDING, REJECTED

    private String remark;

    private Long cardId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // =========================
    // AUTO TIMESTAMP
    // =========================
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();

        // 🔥 SAFETY (avoid null crash)
        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO; // ✅ SAFE
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}