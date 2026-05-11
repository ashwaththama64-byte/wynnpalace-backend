package com.game.platform.controller;

import com.game.platform.entity.*;
import com.game.platform.repository.BetRepository;

import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/bets")
public class BetController {

    private final BetRepository betRepo;

    public BetController(BetRepository betRepo) {
        this.betRepo = betRepo;
    }

    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> error(String msg) {
        return Map.of("success", false, "message", msg);
    }

    // =========================
    // ✅ CLEAN MAPPER
    // =========================
    private Map<String, Object> mapBet(Bet b) {
        Map<String, Object> res = new HashMap<>();

        res.put("id", b.getId());
        res.put("roundId", b.getRound() != null ? b.getRound().getRoundId() : null);

        res.put("totalAmount", b.getTotalAmount());
        res.put("bigBet", b.getBigBet());
        res.put("smallBet", b.getSmallBet());

        // 🔥 IMPORTANT (ADD THIS)
        res.put("betType", b.getBetType());
        res.put("ballIndex", b.getBallIndex()); // ✅ NEW

        res.put("status", b.getStatus());
        res.put("payout", b.getPayout());
        res.put("time", b.getCreatedAt());

        return res;
    }
    // =========================
    // 👤 USER BETS (LIMITED)
    // =========================
    @GetMapping("/me")
    public Map<String, Object> getMyBets(Principal principal) {

        if (principal == null) return error("Unauthorized");

        String username = principal.getName();

        List<Bet> bets =
                betRepo.findTop50ByUser_UsernameOrderByIdDesc(username); // 🔥 limit

        return success(
                bets.stream().map(this::mapBet).toList()
        );
    }

    // =========================
    // 🔥 LATEST
    // =========================
    @GetMapping("/me/latest")
    public Map<String, Object> latest(Principal principal) {

        if (principal == null) return error("Unauthorized");

        String username = principal.getName();

        List<Bet> bets =
                betRepo.findTop10ByUser_UsernameOrderByIdDesc(username);

        return success(
                bets.stream().map(this::mapBet).toList()
        );
    }

    // =========================
    // 📊 ROUND STATS (IMPROVED)
    // =========================
    @GetMapping("/round/{roundId}/stats")
    public Map<String, Object> stats(@PathVariable String roundId) {

        List<Bet> bets = betRepo.findByRound_RoundIdOrderByIdDesc(roundId);

        double totalAmount = 0;

        Map<Integer, Map<String, Integer>> ballStats = new HashMap<>();

        for (Bet b : bets) {

            totalAmount += b.getTotalAmount().doubleValue();

            int ball = b.getBallIndex();
            String type = b.getBetType();

            ballStats.putIfAbsent(ball, new HashMap<>());
            Map<String, Integer> stats = ballStats.get(ball);

            stats.put(type, stats.getOrDefault(type, 0) + 1);
        }

        return success(Map.of(
                "totalBets", bets.size(),
                "totalAmount", totalAmount,
                "ballStats", ballStats // 🔥 per-ball stats
        ));
    }
}