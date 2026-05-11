package com.game.platform.dto;

import java.time.LocalDateTime;

public class AdminControlRequest {

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double minPercent;
    private double maxPercent;

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public double getMinPercent() { return minPercent; }
    public void setMinPercent(double minPercent) { this.minPercent = minPercent; }

    public double getMaxPercent() { return maxPercent; }
    public void setMaxPercent(double maxPercent) { this.maxPercent = maxPercent; }
}