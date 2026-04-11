package com.game.platform.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.game.platform.dto.DashboardResponse;
import com.game.platform.dto.WithdrawRequest;
import com.game.platform.entity.Transaction;
import com.game.platform.entity.User;
import com.game.platform.repository.TransactionRepository;
import com.game.platform.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final TransactionRepository txRepo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepo,
                       TransactionRepository txRepo,
                       PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.txRepo = txRepo;
        this.encoder = encoder;
    }

    // =========================
    // ✅ DASHBOARD (FIXED)
    // =========================
    public DashboardResponse getDashboard(String username) {

        User user = userRepo.findByUsername(username).orElseThrow();

        DashboardResponse res = new DashboardResponse();

        res.setName(user.getUsername());
        res.setUserId(user.getId());
        res.setUserCode(user.getUserCode());
        res.setBalance(user.getBalance());

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> todayTx =
                txRepo.findByUserIdAndCreatedAtBetween(user.getId(), start, end);

        BigDecimal betToday = BigDecimal.ZERO;
        BigDecimal reward = BigDecimal.ZERO;

        for (Transaction tx : todayTx) {
            if ("BET".equals(tx.getType())) {
                betToday = betToday.add(tx.getAmount());
            } else if ("REWARD".equals(tx.getType())) {
                reward = reward.add(tx.getAmount());
            }
        }

        res.setBetToday(betToday);
        res.setProfit(reward.subtract(betToday));

        return res;
    }

    // =========================
    // ✅ ALL TRANSACTIONS
    // =========================
    public List<Transaction> getAllTransactions(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        return txRepo.findByUserId(user.getId())
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
    }

    // =========================
    // ✅ TODAY TRANSACTIONS
    // =========================
    public List<Transaction> getTodayTransactions(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return txRepo.findByUserIdAndCreatedAtBetween(user.getId(), start, end)
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
    }

    // =========================
    // ✅ WEEK TRANSACTIONS
    // =========================
    public List<Transaction> getWeekTransactions(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        return txRepo.findByUserIdAndCreatedAtBetween(user.getId(), start, end)
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
    }

    // =========================
    // 🔥 WITHDRAW REQUEST (FIXED)
    // =========================
    @Transactional
    public void requestWithdraw(String username, WithdrawRequest req) {

        User user = userRepo.findByUsername(username).orElseThrow();

        if (user.getFundPassword() == null ||
            req.getFundPassword() == null ||
            !encoder.matches(req.getFundPassword(), user.getFundPassword())) {
            throw new RuntimeException("Invalid fund password");
        }

        if (req.getAmount() == null ||
            req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        if (user.getBalance().compareTo(req.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        if (req.getCardId() == null) {
            throw new RuntimeException("Select bank card");
        }

        // ✅ DO NOT DEDUCT HERE

        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setAmount(req.getAmount());
        tx.setType("WITHDRAW");
        tx.setStatus("PENDING");
        tx.setCardId(req.getCardId());
        tx.setRemark("Withdraw request");

        txRepo.save(tx);
    }
}