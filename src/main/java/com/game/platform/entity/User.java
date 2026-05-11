package com.game.platform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_usercode", columnList = "userCode")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 UNIQUE USERNAME
    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // 🔐 For withdrawals
    @Column(nullable = false)
    private String fundPassword;

    @Column(unique = true)
    private String userCode;

    // 💰 MONEY FIELD (CRITICAL FIX)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal withdrawable = BigDecimal.ZERO;

    // 🔒 Prevent race conditions (IMPORTANT)
    @Version
    private Long version;

    private LocalDateTime createdAt;

    private String phone;
    private String email;
    private LocalDateTime lastLogin;
   

    @PrePersist
    @PreUpdate
    public void beforeSave() {

        if (this.withdrawable == null) {
            this.withdrawable = BigDecimal.ZERO;
        }

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    // =========================
    // 🔥 BUSINESS METHODS (VERY IMPORTANT)
    // =========================

    public void deductBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void addBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
    
 // 🔥 WITHDRAWABLE METHODS
    public void addWithdrawable(BigDecimal amount) {
        this.withdrawable = this.withdrawable.add(amount);
    }

    public void deductWithdrawable(BigDecimal amount) {
        if (this.withdrawable.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient withdrawable balance");
        }
        this.withdrawable = this.withdrawable.subtract(amount);
    }

    // =========================
    // CONSTRUCTOR
    // =========================

    public User() {}

    // =========================
    // GETTERS & SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getFundPassword() {
        return fundPassword;
    }

    public void setFundPassword(String fundPassword) {
        this.fundPassword = fundPassword;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

	public BigDecimal getWithdrawable() {
		return withdrawable;
	}

	public void setWithdrawable(BigDecimal withdrawable) {
		this.withdrawable = withdrawable;
	}
    
    
}