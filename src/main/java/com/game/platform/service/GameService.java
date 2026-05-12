package com.game.platform.service;

import com.game.platform.entity.*;
import com.game.platform.repository.*;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GameService {

    private final UserRepository userRepo;
    private final BetRepository betRepo;
    private final GameRoundRepository roundRepo;
    private final TransactionRepository txRepo;
    private final AdminControlService adminControlService; // ✅ FIXED
    private final RoundService roundService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameService(UserRepository userRepo,
                       BetRepository betRepo,
                       GameRoundRepository roundRepo,
                       TransactionRepository txRepo,
                       AdminControlService adminControlService,
                       RoundService roundService,
                       SimpMessagingTemplate messagingTemplate) {

        this.userRepo = userRepo;
        this.betRepo = betRepo;
        this.roundRepo = roundRepo;
        this.txRepo = txRepo;
        this.adminControlService = adminControlService;
        this.roundService = roundService;
        this.messagingTemplate = messagingTemplate;
    }

    // =========================
    // 🔥 PLACE BET
    // =========================
    @Transactional
    public void placeMultipleBets(String username, List<Bet> bets, BigDecimal totalAmount) {

        if (bets == null || bets.isEmpty()) {
            throw new RuntimeException("No bets provided");
        }

        GameRound round = roundRepo.findTopBySettledFalseOrderByIdDesc()
                .orElseThrow(() -> new RuntimeException("No active round"));

        if (round.getEndTime().minusSeconds(10).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Betting closed");
        }
     // =========================
     // 🔥 BUILD BALL TYPES MAP
     // =========================
     Map<Integer, List<String>> ballTypes = new HashMap<>();

     for (Bet bet : bets) {

         int ball = bet.getBallIndex();
         String type = bet.getBetType();

         if (type == null) {
             throw new RuntimeException("Bet type required");
         }

         type = type.toUpperCase();
         bet.setBetType(type);

         ballTypes.putIfAbsent(ball, new ArrayList<>());
         ballTypes.get(ball).add(type);
     }
        // =========================
        // ✅ VALIDATIONS
        // =========================
        for (Integer ball : ballTypes.keySet()) {

            List<String> types = ballTypes.get(ball);

            if (types == null) continue;

            if (types.size() > 2) {
                throw new RuntimeException("Only 2 bets allowed per ball (Ball " + ball + ")");
            }

            boolean hasBigSmall =
                types.contains("BIG") || types.contains("SMALL");

            boolean hasSingleDouble =
                types.contains("SINGLE") || types.contains("DOUBLE");

            if (types.size() == 2) {
                if (!(hasBigSmall && hasSingleDouble)) {
                    throw new RuntimeException(
                        "Select 1 from BIG/SMALL and 1 from SINGLE/DOUBLE (Ball " + ball + ")"
                    );
                }
            }

            if (types.contains("BIG") && types.contains("SMALL")) {
                throw new RuntimeException("BIG & SMALL not allowed (Ball " + ball + ")");
            }

            if (types.contains("SINGLE") && types.contains("DOUBLE")) {
                throw new RuntimeException("SINGLE & DOUBLE not allowed (Ball " + ball + ")");
            }
        }
        User user = userRepo.findByUsernameForUpdate(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal totalAvailable = user.getBalance().add(user.getWithdrawable());
        
        BigDecimal finalAmount = totalAmount.multiply(
        	    BigDecimal.valueOf(bets.size())
        	);

        if (totalAvailable.compareTo(finalAmount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        // =========================
        // 💸 DEDUCT BALANCE
        // =========================
        if (user.getBalance().compareTo(finalAmount) >= 0) {
            user.setBalance(user.getBalance().subtract(finalAmount));
        } else {
            BigDecimal remaining = finalAmount.subtract(user.getBalance());
            user.setBalance(BigDecimal.ZERO);
            user.setWithdrawable(user.getWithdrawable().subtract(remaining));
        }
        
        userRepo.save(user);

        // =========================
        // 🔥 CORE FIX HERE
        // =========================

        // total split per bet
        BigDecimal perBet = totalAmount; // ✅ NO SPLIT ACROSS BALLS

        // split inside each bet (Venice logic)
        BigDecimal half = perBet.divide(
                BigDecimal.valueOf(2),
                2,
                RoundingMode.HALF_UP
        );

        for (Bet bet : bets) {

            // ✅ REQUIRED FIELDS (CRITICAL FIX)
            bet.setTotalAmount(perBet);
            bet.setAmountPerOption(perBet); // 🔥 FIX YOUR ERROR

            String type = bet.getBetType();

            if ("BIG".equals(type) || "SMALL".equals(type)) {

                // ✅ split only for BIG/SMALL
                bet.setBigBet(half);
                bet.setSmallBet(half);

            } else {

                // ❌ no split for SINGLE/DOUBLE
                bet.setBigBet(BigDecimal.ZERO);
                bet.setSmallBet(BigDecimal.ZERO);
            }
            bet.setUser(user);
            bet.setRound(round);
            bet.setStatus(BetStatus.PENDING);
            bet.setCreatedAt(LocalDateTime.now());

            betRepo.save(bet);

            messagingTemplate.convertAndSend("/topic/game/bet", bet);
        }

        // =========================
        // 🧾 TRANSACTION
        // =========================
        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setAmount(finalAmount);
        tx.setType(TransactionType.BET);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setRemark("Venice bet");

        txRepo.save(tx);
    }
    // =========================
    // 🔥 SETTLE ROUND
    // =========================
    @Transactional
    public void settleRound(GameRound round) {

        if (round.isSettled()) return;

        List<Bet> bets = betRepo.findByRound(round);

        AdminControl admin = adminControlService.getActiveControl();

        boolean adminMode = false;
        double adminMin = 0;
        double adminMax = 0;

        if (admin != null) {
            adminMode = true;
            adminMin = admin.getMinPercent();
            adminMax = admin.getMaxPercent();
        }
        
        String result;

        if (adminMode) {
            result = chooseSafeResult(bets);
        } else {
            result = roundService.generateResult(round);
        }
        round.setResult(result);

        double totalBet = 0;
        double totalPayout = 0;
        
        for (Bet bet : bets) {

            if (bet.getStatus() != BetStatus.PENDING) continue;

            String type = bet.getBetType();

            if (type == null ||
               (!type.equals("BIG") &&
                !type.equals("SMALL") &&
                !type.equals("SINGLE") &&
                !type.equals("DOUBLE"))) {

                throw new RuntimeException("Invalid bet type");
            }
            BigDecimal total = bet.getTotalAmount();
           
            BigDecimal big = bet.getBigBet();
            BigDecimal small = bet.getSmallBet();
            
            double totalDouble = total.doubleValue();
            double bigDouble = big.doubleValue();
            double smallDouble = small.doubleValue();
            
            totalBet += totalDouble;

            // =========================
            // ❌ FORCE LOSS (SINGLE / DOUBLE)
            // =========================
            if (!adminMode && ("SINGLE".equals(type) || "DOUBLE".equals(type))) {

                bet.setStatus(BetStatus.LOSE);
                bet.setPayout(BigDecimal.ZERO);
                
                betRepo.save(bet);

                continue;
            }

            // =========================
            // 🧠 VENICE CORE LOGIC
            // =========================
            

            boolean isBigWin = "BIG".equals(result);

            double winning = isBigWin ? bigDouble : smallDouble;
            double losing = isBigWin ? smallDouble : bigDouble;
            
            double percent;
            double refund;
            if (adminMode) {

            	double min = adminMin;
            	double max = adminMax;

                percent = randomBetween(min, max);

                double winAmount = winning + (winning * percent / 100);

                 refund = losing * 0.8;

                double totalReturn = winAmount + refund;

                // ✅ guarantee user win within range
                double minReturn = totalDouble * (1 + percent / 100);

                if (totalReturn < minReturn) {
                    totalReturn = minReturn;
                }

                totalPayout += totalReturn;

                User user = userRepo.findByIdForUpdate(bet.getUser().getId()).orElseThrow();
                user.addWithdrawable(BigDecimal.valueOf(totalReturn));
                userRepo.save(user);

                bet.setStatus(BetStatus.WIN);
                bet.setPayout(BigDecimal.valueOf(totalReturn));
                betRepo.save(bet);

                Transaction tx = new Transaction();
                tx.setUserId(user.getId());
                tx.setAmount(BigDecimal.valueOf(totalReturn));
                tx.setType(TransactionType.WIN);
                tx.setStatus(TransactionStatus.SUCCESS);
                tx.setRemark("Admin controlled payout " + percent + "%");

                txRepo.save(tx);

                continue;
            }
           
             else if (round.isBoost()) {
                percent = randomBetween(15, 18);
                refund = losing*0.8; // FULL
            } else {
                percent = randomBetween(15, 18);
                refund = losing * 0.8; // PARTIAL
            }

            double winAmount = winning + (winning * percent / 100);
            double totalReturn = winAmount + refund;

            totalPayout += totalReturn; // ✅ FIX

            // =========================
            // 💰 UPDATE USER
            // =========================
            User user = userRepo.findByIdForUpdate(bet.getUser().getId()).orElseThrow();

            user.addWithdrawable(BigDecimal.valueOf(totalReturn));
            userRepo.save(user);

            // =========================
            // 🧾 UPDATE BET
            // =========================
            double net = totalReturn - totalDouble;

            if (net >= 0) {
                bet.setStatus(BetStatus.WIN);
            } else {
                bet.setStatus(BetStatus.LOSE);
            }
            bet.setPayout(BigDecimal.valueOf(totalReturn));
            betRepo.save(bet);

            // =========================
            // 📄 TRANSACTION
            // =========================
            Transaction tx = new Transaction();
            tx.setUserId(user.getId());
            tx.setAmount(BigDecimal.valueOf(totalReturn));
            tx.setType(TransactionType.WIN);
            tx.setStatus(TransactionStatus.SUCCESS);
            tx.setRemark("Venice payout");

            txRepo.save(tx);
        }
        // =========================
        // ✅ FINALIZE ROUND
        // =========================
        round.setSettled(true);
        roundRepo.save(round);

        System.out.println("💰 TOTAL BET: " + totalBet);
        System.out.println("💰 TOTAL PAYOUT: " + totalPayout);
        System.out.println("💰 PROFIT: " + (totalBet - totalPayout));

        messagingTemplate.convertAndSend("/topic/game/result", round);
    }

    // =========================
    // 🧠 SAFE RESULT
    // =========================
    private String chooseSafeResult(List<Bet> bets) {

        double payoutBig = 0;
        double payoutSmall = 0;

        for (Bet bet : bets) {

            String type = bet.getBetType();

            // ❌ ignore SINGLE / DOUBLE
            if ("SINGLE".equals(type) || "DOUBLE".equals(type)) continue;

            double total = bet.getTotalAmount().doubleValue();
            double big = total / 2;
            double small = total / 2;

            payoutBig += (big * 1.15 + small * 0.8);
            payoutSmall += (small * 1.15 + big * 0.8);
        }

        return payoutBig < payoutSmall ? "BIG" : "SMALL";
    }
    private double randomBetween(double min, double max) {
        return min + (Math.random() * (max - min));
    }
    
    
}