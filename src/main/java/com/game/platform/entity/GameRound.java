package com.game.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "game_round", indexes = {
        @Index(name = "idx_round_settled", columnList = "settled"),
        @Index(name = "idx_round_time", columnList = "startTime,endTime")
})
public class GameRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ ROUND DISPLAY ID (2026-0001)
    @Column(unique = true, nullable = false)
    private String roundId;

    @Column(unique = true)
    private Long displayNumber;
    
    // ✅ RESULT → BIG / SMALL
    private String result;

    // ✅ BOOST ROUND FLAG
    private boolean boost;

    // ✅ LUCKY NUMBER (0–9999)
    private int luckyNumber;

    // ✅ SERVER SEED (for deterministic result)
    private String serverSeed;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime endTime;

    private boolean settled;

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() { return id; }

    public String getRoundId() { return roundId; }
    public void setRoundId(String roundId) { this.roundId = roundId; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public boolean isBoost() { return boost; }
    public void setBoost(boolean boost) { this.boost = boost; }

    public int getLuckyNumber() { return luckyNumber; }
    public void setLuckyNumber(int luckyNumber) { this.luckyNumber = luckyNumber; }

    public String getServerSeed() { return serverSeed; }
    public void setServerSeed(String serverSeed) { this.serverSeed = serverSeed; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }

	public Long getDisplayNumber() {
		return displayNumber;
	}

	public void setDisplayNumber(Long displayNumber) {
		this.displayNumber = displayNumber;
	}
    
    
}