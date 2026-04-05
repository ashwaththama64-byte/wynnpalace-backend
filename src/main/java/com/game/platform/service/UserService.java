package com.game.platform.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.game.platform.entity.Transaction;
import com.game.platform.entity.User;
import com.game.platform.repository.TransactionRepository;
import com.game.platform.repository.UserRepository;

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

    // ✅ DASHBOARD
    public User getDashboard(String username) {
        return userRepo.findByUsername(username).orElseThrow();
    }

    // ✅ ALL TRANSACTIONS
    public List<Transaction> getAllTransactions(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return txRepo.findByUserId(user.getId());
    }

    // ✅ FILTER: TODAY
    public List<Transaction> getTodayTransactions(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return txRepo.findByUserIdAndCreatedAtBetween(user.getId(), start, end);
    }

    // ✅ FILTER: THIS WEEK
    public List<Transaction> getWeekTransactions(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        return txRepo.findByUserIdAndCreatedAtBetween(user.getId(), start, end);
    }

    // 🔥 WITHDRAW REQUEST
    public void requestWithdraw(String username, Double amount, String fundPassword) {

        User user = userRepo.findByUsername(username).orElseThrow();

        // verify fund password
        if (!encoder.matches(fundPassword, user.getFundPassword())) {
            throw new RuntimeException("Invalid fund password");
        }

        if (user.getBalance() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // DO NOT deduct yet (admin approval required)

        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setAmount(amount);
        tx.setType("WITHDRAW");
        tx.setStatus("PENDING");
        tx.setRemark("Withdraw request");

        txRepo.save(tx);
    }
}