package com.game.platform.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.game.platform.entity.Bet;

import java.math.BigDecimal;

public interface ProfitRepository extends CrudRepository<Bet, Long> {

    @Query("SELECT SUM(b.totalAmount - b.payout) FROM Bet b")
    BigDecimal getTotalProfit();

    @Query("SELECT SUM(b.totalAmount) FROM Bet b")
    BigDecimal getTotalBet();

    @Query("SELECT SUM(b.payout) FROM Bet b")
    BigDecimal getTotalPayout();
}