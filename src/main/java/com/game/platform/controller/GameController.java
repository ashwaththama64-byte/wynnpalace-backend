package com.game.platform.controller;

import com.game.platform.dto.BetRequest;
import com.game.platform.entity.Bet;
import com.game.platform.entity.GameRound;
import com.game.platform.repository.GameRoundRepository;
import com.game.platform.service.GameService;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;
    private final GameRoundRepository roundRepo;

    public GameController(GameService gameService,
                          GameRoundRepository roundRepo) {
        this.gameService = gameService;
        this.roundRepo = roundRepo;
    }

    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> error(String msg) {
        return Map.of("success", false, "message", msg);
    }

    // =========================
    // 🔥 PLACE BET
    // =========================
    @PostMapping("/bet")
    public Map<String, Object> placeBet(@RequestBody BetRequest req,
                                        Principal principal) {

        try {
            if (principal == null) return error("Unauthorized");

            String username = principal.getName();

            // ✅ VALIDATION
            if (req.getTotalAmount() == null ||
                req.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return error("Invalid amount");
            }

            if (req.getBets() == null || req.getBets().isEmpty()) {
                return error("No bets provided");
            }

            // ✅ CHECK ROUND
            GameRound round = roundRepo
                    .findTopBySettledFalseOrderByIdDesc()
                    .orElse(null);

            if (round == null) return error("No active round");

            if (LocalDateTime.now().isAfter(round.getEndTime().minusSeconds(10))) {
                return error("Betting closed");
            }

            // ✅ CONVERT DTO → ENTITY
            List<Bet> bets = new ArrayList<>();

            for (BetRequest.SingleBet b : req.getBets()) {

                if (b.getBetType() == null) {
                    return error("Bet type required");
                }

                String type = b.getBetType().toUpperCase();

                if (!type.equals("BIG") &&
                    !type.equals("SMALL") &&
                    !type.equals("SINGLE") &&
                    !type.equals("DOUBLE")) {
                    return error("Invalid bet type");
                }

                if (b.getBallIndex() < 1 || b.getBallIndex() > 5) {
                    return error("Invalid ball index");
                }

                Bet bet = new Bet();
                bet.setBetType(type);
                bet.setBallIndex(b.getBallIndex());

                bets.add(bet);
            }

            // ✅ CALL NEW SERVICE
            gameService.placeMultipleBets(
                    username,
                    bets,
                    req.getTotalAmount()
            );

            return success(Map.of(
                    "totalAmount", req.getTotalAmount(),
                    "bets", bets.size(),
                    "message", "Bet placed"
            ));

        } catch (Exception e) {
            return error(e.getMessage());
        }
    }
    // =========================
    // 🔥 CURRENT ROUND
    // =========================
    @GetMapping("/round/current")
    public Map<String, Object> currentRound() {

        GameRound round = roundRepo
                .findTopBySettledFalseOrderByIdDesc()
                .orElse(null);

        return success(round);
    }
}