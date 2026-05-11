package com.game.platform.scheduler;

import com.game.platform.entity.GameRound;
import com.game.platform.repository.GameRoundRepository;
import com.game.platform.service.GameService;
import com.game.platform.service.RoundService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class GameScheduler {

    private final GameRoundRepository roundRepo;
    private final GameService gameService;
    private final RoundService roundService;

    public GameScheduler(GameRoundRepository roundRepo,
                         GameService gameService,
                         RoundService roundService) {
        this.roundRepo = roundRepo;
        this.gameService = gameService;
        this.roundService = roundService;
    }

    // 🔥 RUN EVERY 2 SECONDS (ENGINE LOOP)
    @Scheduled(fixedRate = 2000)
    public synchronized void runGameLoop() {

        try {

            LocalDateTime now = LocalDateTime.now();

            GameRound round = roundRepo
                    .findTopBySettledFalseOrderByIdDesc()
                    .orElse(null);

            // 🟢 NO ROUND → CREATE
            if (round == null) {
                roundService.createNewRound();
                return;
            }

            // 🔴 ROUND EXPIRED → SETTLE + NEW
            if (!round.isSettled() && now.isAfter(round.getEndTime())) {

                System.out.println("🔄 Settling round: " + round.getRoundId());

                // 1️⃣ Generate result (if not already)
                roundService.generateOrGetResult(round);

                // 2️⃣ Settle (payout engine)
                gameService.settleRound(round);

                // 3️⃣ Create next round
                roundService.createNewRound();

                return;
            }

            // ⏳ still active → do nothing

        } catch (Exception e) {
            System.out.println("❌ Scheduler error: " + e.getMessage());
        }
    }
}