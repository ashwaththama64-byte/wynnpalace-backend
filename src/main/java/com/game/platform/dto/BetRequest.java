package com.game.platform.dto;

import java.math.BigDecimal;
import java.util.List;

public class BetRequest {

    private BigDecimal totalAmount;
    private List<SingleBet> bets;

    public static class SingleBet {
        private String betType;
        private int ballIndex;

        public String getBetType() { return betType; }
        public void setBetType(String betType) { this.betType = betType; }

        public int getBallIndex() { return ballIndex; }
        public void setBallIndex(int ballIndex) { this.ballIndex = ballIndex; }
    }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<SingleBet> getBets() { return bets; }
    public void setBets(List<SingleBet> bets) { this.bets = bets; }
}