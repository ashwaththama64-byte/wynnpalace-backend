package com.game.platform.controller;

import com.game.platform.dto.ResultDTO;
import com.game.platform.entity.GameRound;
import com.game.platform.repository.GameRoundRepository;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/results")
public class GameResultController {

    private final GameRoundRepository roundRepo;

    public GameResultController(GameRoundRepository roundRepo) {
        this.roundRepo = roundRepo;
    }

    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "data", data);
    }

    // =========================
    // 🔥 LAST 10 RESULTS
    @GetMapping("/recent")
    public Map<String, Object> recent() {

        List<GameRound> rounds =
                roundRepo.findTop10BySettledTrueOrderByIdDesc();

        List<ResultDTO> result = new ArrayList<>();

        for (GameRound r : rounds) {

            int displayNumber = 20140 + r.getId().intValue();

            result.add(new ResultDTO(
                    r.getRoundId(),
                    r.isBoost(),
                    r.getLuckyNumber(),
                    displayNumber
            ));
        }

        return success(result);
    }

    // =========================
    // 🔥 LATEST RESULT (FIXED)
    // =========================
    @GetMapping("/latest")
    public Map<String, Object> latest() {

        GameRound round =
                roundRepo.findTopBySettledTrueOrderByIdDesc().orElse(null);

        if (round == null) {
            return success(null);
        }

        return success(new ResultDTO(
                round.getRoundId(),
                round.isBoost(),
                round.getLuckyNumber(),
                round.getDisplayNumber()
        ));
    }

    // =========================
    // 🔥 CURRENT RUNNING ROUND
    // =========================
    @GetMapping("/current-round")
    public Map<String, Object> currentRound() {

        GameRound round =
                roundRepo.findTopBySettledFalseOrderByIdDesc().orElse(null);

        return success(round);
    }
}