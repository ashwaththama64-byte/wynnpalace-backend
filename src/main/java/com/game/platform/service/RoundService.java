package com.game.platform.service;

import com.game.platform.entity.GameRound;
import com.game.platform.repository.GameRoundRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class RoundService {

    private final GameRoundRepository roundRepo;
    private final SimpMessagingTemplate messagingTemplate;

    // 🔢 ROUND COUNTER
    private static int roundCounter = 1;

    // 🔥 ADD HERE (CACHE)
    private GameRound cachedRound;
    private long lastFetch = 0;

    public RoundService(GameRoundRepository roundRepo,
                        SimpMessagingTemplate messagingTemplate) {
        this.roundRepo = roundRepo;
        this.messagingTemplate = messagingTemplate;
    }

    // =========================
    // 🔥 GET OR CREATE ACTIVE ROUND
    // =========================
    
    
    public synchronized GameRound getOrCreateActiveRound() {

        long now = System.currentTimeMillis();

        if (cachedRound != null && (now - lastFetch) < 2000) {
            return cachedRound;
        }

        GameRound round = roundRepo
                .findTopBySettledFalseOrderByIdDesc()
                .orElseGet(this::createNewRound);

        cachedRound = round;
        lastFetch = now;

        return round;
    }

    // =========================
    // 🔥 CREATE NEW ROUND
    // =========================
    public GameRound createNewRound() {

        LocalDateTime now = LocalDateTime.now();

        GameRound round = new GameRound();

        // ✅ CLEAN ROUND ID (2026-0001)
        int year = now.getYear();
        long count = roundRepo.count() + 1;
        String roundId = year +"-"+ String.format("%04d", count);
        round.setRoundId(roundId);

        // ⏱ TIME
        round.setStartTime(now);
        round.setEndTime(now.plusMinutes(5));

        // 🎯 BOOST (10%)
        round.setBoost(Math.random() < 0.5);

        // 🔢 LUCKY NUMBER (0–9999)
        round.setLuckyNumber(new Random().nextInt(10000));

        // 🔐 SERVER SEED (ROTATES EVERY ROUND)
        round.setServerSeed("VENICE_" + System.nanoTime());

        // STATE
        round.setSettled(false);
        round.setResult(null);

        GameRound saved = roundRepo.save(round);

        System.out.println("🆕 NEW ROUND: " + saved.getRoundId());

        messagingTemplate.convertAndSend("/topic/game/round", saved);

        return saved;
    }

    // =========================
    // 🎲 GENERATE RESULT (DETERMINISTIC)
    // =========================
    public String generateResult(GameRound round) {

        try {
            if (round.getRoundId() == null || round.getServerSeed() == null) {
                throw new RuntimeException("Round data invalid: roundId or seed is null");
            }

            String input = round.getRoundId() + round.getServerSeed();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(input.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }

            String first8 = hex.substring(0, 8);

            long number = Long.parseLong(first8, 16) % 100;

            return number < 50 ? "SMALL" : "BIG";

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 IMPORTANT
            throw new RuntimeException("Hash generation failed");
        }
    }

    // =========================
    // 🔥 SET RESULT IF NOT EXISTS
    // =========================
    public GameRound generateOrGetResult(GameRound round) {

        if (round.getResult() != null) {
            return round;
        }

        String result = generateResult(round);

        round.setResult(result);
       

        GameRound updated = roundRepo.save(round);

        messagingTemplate.convertAndSend("/topic/game/result", updated);

        return updated;
    }

    // =========================
    // 🔥 ADMIN OVERRIDE
    // =========================
    public GameRound overrideResult(Long roundId, String result) {

        if (!result.equals("BIG") && !result.equals("SMALL")) {
            throw new RuntimeException("Invalid result");
        }

        GameRound round = roundRepo.findById(roundId)
                .orElseThrow(() -> new RuntimeException("Round not found"));

        if (round.isSettled()) {
            throw new RuntimeException("Cannot override settled round");
        }

        round.setResult(result);

        GameRound updated = roundRepo.save(round);

        messagingTemplate.convertAndSend("/topic/game/result", updated);

        return updated;
    }
}