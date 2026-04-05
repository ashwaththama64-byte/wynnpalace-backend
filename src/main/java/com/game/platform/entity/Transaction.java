package com.game.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String type; // RECHARGE, WITHDRAW, BET, REWARD

    private Double amount;

    private String status; // SUCCESS, PENDING, REJECTED

    private String remark;

    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== getters & setters =====

    public Long getId() { return id; }

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }

    public void setType(String type) { this.type = type; }

    public Double getAmount() { return amount; }

    public void setAmount(Double amount) { this.amount = amount; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getRemark() { return remark; }

    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}