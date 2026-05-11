package com.game.platform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_tx_user", columnList = "userId"),
        @Index(name = "idx_tx_type", columnList = "type"),
        @Index(name = "idx_tx_time", columnList = "createdAt")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ USER
    @Column(nullable = false)
    private Long userId;

    // ✅ TYPE (STRICT ENUM)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    // ✅ AMOUNT
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    // ✅ STATUS (STRICT ENUM)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    // ✅ OPTIONAL INFO
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

        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
    }

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Long getCardId() { return cardId; }
    public void setCardId(Long cardId) { this.cardId = cardId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}