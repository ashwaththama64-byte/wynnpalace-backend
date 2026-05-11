package com.game.platform.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.game.platform.dto.AdminChangeCredentialsRequest;
import com.game.platform.dto.UserResponse;
import com.game.platform.dto.WithdrawAdminResponse;
import com.game.platform.entity.*;
import com.game.platform.repository.*;
import com.game.platform.util.JwtUtil;

import jakarta.transaction.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final TransactionRepository txRepo;
    private final BankCardRepository cardRepo;

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminService(UserRepository userRepo,
                        TransactionRepository txRepo,
                        BankCardRepository cardRepo,
                        AdminRepository adminRepo,
                        PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil) {

        this.userRepo = userRepo;
        this.txRepo = txRepo;
        this.cardRepo = cardRepo;
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // =========================
    // 🔐 ADMIN LOGIN
    // =========================
    public String login(String username, String password) {

        Admin admin = adminRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return jwtUtil.generateAdminToken(admin.getUsername());
    }

    // =========================
    // 👤 GET ALL USERS
    // =========================
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream().map(user -> {
            UserResponse res = new UserResponse();
            res.setId(user.getId());
            res.setUsername(user.getUsername());
            res.setUserCode(user.getUserCode());
            res.setBalance(user.getBalance());
            res.setWithdrawable(user.getWithdrawable());
            return res;
        }).toList();
    }

    // =========================
    // 💰 RECHARGE
    // =========================
    @Transactional
    public void recharge(Long userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.addBalance(amount);
        userRepo.save(user);

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(TransactionType.RECHARGE);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setRemark("Admin recharge");

        txRepo.save(tx);
    }

    // =========================
    // ✅ APPROVE WITHDRAW
    // =========================
    @Transactional
    public void approveWithdraw(Long txId) {

        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Already processed");
        }

        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setRemark("Withdraw approved");

        txRepo.save(tx);
    }

    // =========================
    // ❌ REJECT WITHDRAW
    // =========================
    @Transactional
    public void rejectWithdraw(Long txId) {

        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new RuntimeException("Already processed");
        }

        User user = userRepo.findById(tx.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔄 Refund to withdrawable
        user.addWithdrawable(tx.getAmount());
        userRepo.save(user);

        tx.setStatus(TransactionStatus.FAILED);
        tx.setRemark("Withdraw rejected & refunded");

        txRepo.save(tx);
    }

    // =========================
    // 📋 PENDING WITHDRAWS
    // =========================
    public List<WithdrawAdminResponse> getPendingWithdraws() {

        List<Transaction> list = txRepo.findByStatus(TransactionStatus.PENDING);

        return list.stream().map(tx -> {

            BankCard card = null;

            if (tx.getCardId() != null) {
                card = cardRepo.findById(tx.getCardId()).orElse(null);
            }

            WithdrawAdminResponse res = new WithdrawAdminResponse();

            res.setTxId(tx.getId());
            res.setUserId(tx.getUserId());
            res.setAmount(tx.getAmount());
            res.setStatus(tx.getStatus().name());

            if (card != null) {
                res.setBankName(card.getBankName());
                res.setAccountNumber(card.getAccountNumber());
                res.setHolderName(card.getHolderName());
                res.setIfsc(card.getIfsc());
            }

            return res;

        }).toList();
    }

    // =========================
    // 🔍 SEARCH USERS
    // =========================
    public List<UserResponse> searchUsers(String keyword) {

        return userRepo.searchByUsernameOrUserCode(keyword)
                .stream()
                .map(user -> {
                    UserResponse res = new UserResponse();
                    res.setId(user.getId());
                    res.setUsername(user.getUsername());
                    res.setUserCode(user.getUserCode());
                    res.setBalance(user.getBalance());
                    res.setWithdrawable(user.getWithdrawable());
                    return res;
                }).toList();
    }
    @Transactional
    public void changeCredentials(AdminChangeCredentialsRequest req) {

        // 1️⃣ Find admin by old username
        Admin admin = adminRepo.findByUsername(req.getOldUsername())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // 2️⃣ Verify old password
        if (!passwordEncoder.matches(req.getOldPassword(), admin.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // 3️⃣ Check new password match
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // 4️⃣ Update username (optional)
        if (req.getNewUsername() != null && !req.getNewUsername().isBlank()) {

            // check duplicate
            adminRepo.findByUsername(req.getNewUsername()).ifPresent(a -> {
                throw new RuntimeException("Username already taken");
            });

            admin.setUsername(req.getNewUsername());
        }

        // 5️⃣ Update password
        admin.setPassword(passwordEncoder.encode(req.getNewPassword()));

        adminRepo.save(admin);
    }
}