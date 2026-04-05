package com.game.platform.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.game.platform.entity.Transaction;
import com.game.platform.entity.User;
import com.game.platform.repository.TransactionRepository;
import com.game.platform.repository.UserRepository;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final TransactionRepository txRepo;

    public AdminService(UserRepository userRepo,
                        TransactionRepository txRepo) {
        this.userRepo = userRepo;
        this.txRepo = txRepo;
    }

    // 🔥 GET ALL USERS (IMPORTANT)
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    // ✅ RECHARGE
    public void recharge(Long userId, Double amount) {

        if (amount == null || amount <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setBalance(user.getBalance() + amount);
        userRepo.save(user);

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType("RECHARGE");
        tx.setStatus("SUCCESS");
        tx.setRemark("Admin recharge");

        txRepo.save(tx);
    }

    // 🔥 APPROVE WITHDRAW
    public void approveWithdraw(Long txId) {

        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!"PENDING".equals(tx.getStatus())) {
            throw new RuntimeException("Already processed");
        }

        User user = userRepo.findById(tx.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBalance() < tx.getAmount()) {
            throw new RuntimeException("Insufficient balance");
        }

        user.setBalance(user.getBalance() - tx.getAmount());
        userRepo.save(user);

        tx.setStatus("SUCCESS");
        tx.setRemark("Withdraw approved");
        txRepo.save(tx);
    }

    // ❌ REJECT WITHDRAW
    public void rejectWithdraw(Long txId) {

        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        tx.setStatus("REJECTED");
        tx.setRemark("Withdraw rejected");

        txRepo.save(tx);
    }

    // 🔥 GET PENDING WITHDRAWS
    public List<Transaction> getPendingWithdraws() {
        return txRepo.findByStatus("PENDING");
    }
    
    public List<User> searchUsers(String keyword) {
        return userRepo.findByUsernameContaining(keyword);
    }
}