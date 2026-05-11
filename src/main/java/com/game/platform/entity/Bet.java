package com.game.platform.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bet", indexes = {
	    @Index(name = "idx_bet_user", columnList = "user_id"),
	    @Index(name = "idx_bet_round", columnList = "round_id"),
	    @Index(name = "idx_bet_status", columnList = "status"),
	    @Index(name = "idx_bet_type", columnList = "betType") // 🔥 ADD
	})
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private int ballIndex; // 1 to 5
    
    @Column(nullable = false)
    private String betType; // BIG / SMALL / SINGLE / DOUBLE

    // 🔥 USER RELATION
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 🔥 ROUND RELATION
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", nullable = false)
    private GameRound round;
    
    @Column(name = "amount_per_option", nullable = false, precision = 19, scale = 2)
    private BigDecimal amountPerOption;
    
    // ✅ TOTAL BET AMOUNT (User input)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    // ✅ AUTO SPLIT VALUES
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal bigBet;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal smallBet;

    // ✅ FINAL PAYOUT
    @Column(precision = 19, scale = 2)
    private BigDecimal payout;

    // ✅ STATUS
    @Enumerated(EnumType.STRING)
    private BetStatus status;

    private LocalDateTime createdAt;

    // =========================
    // AUTO TIMESTAMP
    // =========================
    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public GameRound getRound() { return round; }
    public void setRound(GameRound round) { this.round = round; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getBigBet() { return bigBet; }
    public void setBigBet(BigDecimal bigBet) { this.bigBet = bigBet; }

    public BigDecimal getSmallBet() { return smallBet; }
    public void setSmallBet(BigDecimal smallBet) { this.smallBet = smallBet; }

    public BigDecimal getPayout() { return payout; }
    public void setPayout(BigDecimal payout) { this.payout = payout; }

    public BetStatus getStatus() { return status; }
    public void setStatus(BetStatus status) { this.status = status; }
    
    public String getBetType() {
        return betType;
    }

    public void setBetType(String betType) {
        this.betType = betType;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }

	public int getBallIndex() {
		return ballIndex;
	}

	public void setBallIndex(int ballIndex) {
		this.ballIndex = ballIndex;
	}

	public BigDecimal getAmountPerOption() {
		return amountPerOption;
	}

	public void setAmountPerOption(BigDecimal amountPerOption) {
		this.amountPerOption = amountPerOption;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
	
}