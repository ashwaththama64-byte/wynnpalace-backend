package com.game.platform.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.game.platform.dto.UserResponse;
import com.game.platform.dto.WithdrawAdminResponse;
import com.game.platform.entity.Admin;
import com.game.platform.entity.BankCard;
import com.game.platform.entity.Transaction;
import com.game.platform.entity.User;
import com.game.platform.repository.AdminRepository;
import com.game.platform.repository.BankCardRepository;
import com.game.platform.repository.TransactionRepository;
import com.game.platform.repository.UserRepository;
import com.game.platform.security.JwtService;
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
    
    public String login(String username, String password) {

        System.out.println("🔐 ADMIN LOGIN START");

        Admin admin = adminRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // ✅ FIXED
        String token = jwtUtil.generateAdminToken(admin.getUsername());

        System.out.println("GENERATING TOKEN ROLE: ROLE_ADMIN");

        return token;
    }

    // =========================
    // 🔥 GET ALL USERS
    // =========================
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream().map(user -> {
            UserResponse res = new UserResponse();
            res.setId(user.getId());
            res.setUsername(user.getUsername());
            res.setUserCode(user.getUserCode());
            res.setBalance(user.getBalance());
            return res;
        }).toList();
    }

    // =========================
    // ✅ RECHARGE (FIXED)
    // =========================
    @Transactional
    public void recharge(Long userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ FIX
        user.setBalance(user.getBalance().add(amount));
        userRepo.save(user);

        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType("RECHARGE");
        tx.setStatus("SUCCESS");
        tx.setRemark("Admin recharge");

        txRepo.save(tx);
    }

    // =========================
    // 🔥 APPROVE WITHDRAW (FIXED)
    // =========================
    @Transactional
    public void approveWithdraw(Long txId) {

        Transaction tx = txRepo.findById(txId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (!"PENDING".equals(tx.getStatus())) {
            throw new RuntimeException("Already processed");
        }

        User user = userRepo.findById(tx.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ FIX (BigDecimal compare)
        if (user.getBalance().compareTo(tx.getAmount()) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        // ✅ FIX (subtract)
        user.setBalance(user.getBalance().subtract(tx.getAmount()));
        userRepo.save(user);

        tx.setStatus("SUCCESS");
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

        tx.setStatus("REJECTED");
        tx.setRemark("Withdraw rejected");

        txRepo.save(tx);
    }

    // =========================
    // 🔥 GET PENDING WITHDRAWS
    // =========================
    public List<WithdrawAdminResponse> getPendingWithdraws() {

        List<Transaction> list = txRepo.findByStatus("PENDING");

        return list.stream().map(tx -> {

            BankCard card = null;

            if (tx.getCardId() != null) {
                card = cardRepo.findById(tx.getCardId()).orElse(null);
            }

            WithdrawAdminResponse res = new WithdrawAdminResponse();

            res.setTxId(tx.getId());
            res.setUserId(tx.getUserId());
            res.setAmount(tx.getAmount());
            res.setStatus(tx.getStatus());

            if (card != null) {
                res.setBankName(card.getBankName());
                res.setAccountNumber(mask(card.getAccountNumber()));
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
                    return res;
                }).toList();
    }

    // =========================
    // 🔐 MASK ACCOUNT NUMBER
    // =========================
    private String mask(String acc) {
        if (acc == null || acc.length() < 4) return "****";
        return "****" + acc.substring(acc.length() - 4);
    }
}