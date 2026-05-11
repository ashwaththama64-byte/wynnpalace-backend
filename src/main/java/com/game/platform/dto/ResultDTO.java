package com.game.platform.dto;

public class ResultDTO {

    private String roundId;
    private boolean boost;
    private int luckyNumber;
    private Long displayNumber;

    public ResultDTO(String roundId, boolean boost, int luckyNumber,long displayNumber) {
        this.roundId = roundId;
        this.boost = boost;
        this.luckyNumber = luckyNumber;
        this.displayNumber = displayNumber;
    }

    public String getRoundId() { return roundId; }
    public boolean isBoost() { return boost; }
    public int getLuckyNumber() { return luckyNumber; }

	public Long getDisplayNumber() {
		return displayNumber;
	}

	public void setDisplayNumber(Long displayNumber) {
		this.displayNumber = displayNumber;
	}
    
}