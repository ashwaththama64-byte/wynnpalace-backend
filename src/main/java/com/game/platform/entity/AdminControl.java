package com.game.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class AdminControl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ PROFIT WINDOW
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;

    // ✅ ENABLE / DISABLE
    private boolean active = false;

    // ✅ CONTROL PROFIT %
    private double minPercent; // e.g. 15
    private double maxPercent; // e.g. 18

    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() { return id; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public double getMinPercent() { return minPercent; }
    public void setMinPercent(double minPercent) { this.minPercent = minPercent; }

    public double getMaxPercent() { return maxPercent; }
    public void setMaxPercent(double maxPercent) { this.maxPercent = maxPercent; }
}