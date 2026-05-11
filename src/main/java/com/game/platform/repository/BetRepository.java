package com.game.platform.repository;

import com.game.platform.entity.Bet;
import com.game.platform.entity.BetStatus;
import com.game.platform.entity.GameRound;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface BetRepository extends JpaRepository<Bet, Long> {

    // =========================
    // 💰 TOTAL BET
    // =========================
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bet b WHERE b.round.id = :roundId")
    BigDecimal getTotalBetAmount(Long roundId);

    // =========================
    // 💰 TOTAL PAYOUT
    // =========================
    @Query("SELECT COALESCE(SUM(b.payout), 0) FROM Bet b WHERE b.round.id = :roundId")
    BigDecimal getTotalPayout(Long roundId);

    // =========================
    // 👤 USER BETS
    // =========================
    List<Bet> findByUser_UsernameOrderByIdDesc(String username);

    List<Bet> findTop10ByUser_UsernameOrderByIdDesc(String username);

    List<Bet> findTop50ByUser_UsernameOrderByIdDesc(String username);

    // =========================
    // 🔥 ROUND BETS
    // =========================
    List<Bet> findByRound_RoundIdOrderByIdDesc(String roundId);

    List<Bet> findByRound(GameRound round);

    // =========================
    // 🔥 STATUS FILTER
    // =========================
    List<Bet> findByStatus(BetStatus status);
}