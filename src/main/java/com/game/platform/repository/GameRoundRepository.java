package com.game.platform.repository;

import com.game.platform.entity.GameRound;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRoundRepository extends JpaRepository<GameRound, Long> {

    // =========================
    // 🔥 ACTIVE ROUND
    // =========================
    Optional<GameRound> findTopBySettledFalseOrderByIdDesc();

    // =========================
    // 🔥 LATEST ROUND (ANY)
    // =========================
    Optional<GameRound> findTopByOrderByIdDesc();

    // =========================
    // 🔥 LATEST RESULT (IMPORTANT)
    // =========================
    Optional<GameRound> findTopBySettledTrueOrderByIdDesc();

    // =========================
    // 🔥 RECENT RESULTS (UI)
    // =========================
    List<GameRound> findTop5BySettledTrueOrderByIdDesc();

    List<GameRound> findTop10BySettledTrueOrderByIdDesc();

    List<GameRound> findTop20BySettledTrueOrderByIdDesc();

    // =========================
    // 🔥 ALL RECENT ROUNDS
    // =========================
    List<GameRound> findTop20ByOrderByIdDesc();
}