package com.game.platform.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.game.platform.dto.ChangeFundPasswordRequest;
import com.game.platform.dto.ChangePasswordRequest;
import com.game.platform.dto.DashboardResponse;
import com.game.platform.dto.UpdateProfileRequest;
import com.game.platform.dto.WithdrawRequest;
import com.game.platform.entity.Transaction;
import com.game.platform.entity.TransactionStatus;
import com.game.platform.entity.TransactionType;
import com.game.platform.entity.User;
import com.game.platform.repository.TransactionRepository;
import com.game.platform.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepo;
    private final TransactionRepository txRepo;
    private final PasswordEncoder encoder;

    private final BankService bankService;

    public UserService(UserRepository userRepo,
                       TransactionRepository txRepo,
                       PasswordEncoder encoder,
                       BankService bankService) {
        this.userRepo = userRepo;
        this.txRepo = txRepo;
        this.encoder = encoder;
        this.bankService = bankService;
    }

    // =========================
    // ✅ DASHBOARD (FIXED)
    // =========================
    @Transactional
    public DashboardResponse getDashboard(String username) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User NOT FOUND"));

        DashboardResponse res = new DashboardResponse();

        res.setName(user.getUsername());
        res.setUserId(user.getId());
        res.setUserCode(user.getUserCode());
        res.setBalance(user.getBalance());
        res.setWithdrawable(
            user.getWithdrawable() != null ? user.getWithdrawable() : BigDecimal.ZERO
        );

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

        List<Transaction> list = txRepo.findByUserId(user.getId());

        System.out.println("🔥 TX COUNT: " + list.size());

        for (Transaction tx : list) {
            System.out.println("TX -> " + tx.getId() + " | " + tx.getCreatedAt());
        }

        return list.stream()
        		.filter(tx -> tx.getCreatedAt() != null) 
            .sorted(Comparator.comparing(
                Transaction::getCreatedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed())
            .toList();
    }

    // =========================
    // ✅ TODAY TRANSACTIONS
    // =========================
   
    public List<Transaction> getTodayTransactions(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        return txRepo.findByUserId(user.getId())
                .stream()
                .filter(tx -> tx.getCreatedAt() != null)
                .filter(tx -> !tx.getCreatedAt().isBefore(start)
                           && !tx.getCreatedAt().isAfter(end))
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

        return txRepo.findByUserId(user.getId())
                .stream()
                .filter(tx -> tx.getCreatedAt() != null)
                .filter(tx -> !tx.getCreatedAt().isBefore(start)
                           && !tx.getCreatedAt().isAfter(end))
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .toList();
    }

    // =========================
    // 🔥 WITHDRAW REQUEST (FIXED)
    // =========================
    public void requestWithdraw(String username, WithdrawRequest req) {

        User user = userRepo.findByUsername(username).orElseThrow();

        if (!encoder.matches(req.getFundPassword(), user.getFundPassword())) {
            throw new RuntimeException("Invalid fund password");
        }

        if (req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid amount");
        }

        if (req.getAmount().compareTo(user.getWithdrawable()) > 0) {
            throw new RuntimeException("Exceeds withdrawable");
        }

        // ✅ DEDUCT ONLY FROM WITHDRAWABLE
        user.deductWithdrawable(req.getAmount());
        userRepo.save(user);

        // ✅ CREATE TX
        Transaction tx = new Transaction();
        tx.setUserId(user.getId());
        tx.setAmount(req.getAmount());
        tx.setType(TransactionType.WITHDRAW);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setCardId(req.getCardId());
        tx.setRemark("Withdraw request");

        txRepo.save(tx);
    }
    
    
    public Map<String, Object> getProfile(String username) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> res = new HashMap<>();

        res.put("username", user.getUsername());
        res.put("email", user.getEmail());
        res.put("phone", user.getPhone());
        res.put("userCode", user.getUserCode());
        res.put("balance", user.getBalance());
        res.put("withdrawable", user.getWithdrawable()); // ✅ ADD THIS
        res.put("lastLogin", user.getLastLogin());

        // 🔥 SAFE BANK CHECK
        try {
            boolean hasBank = !bankService.getCards(username).isEmpty();
            res.put("hasBank", hasBank);
        } catch (Exception e) {
            res.put("hasBank", false); // ✅ PREVENT CRASH
        }

        return res;
    }
    
    public void changePassword(String username, ChangePasswordRequest req) {

        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔐 verify old password
        if (!encoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // 🔥 validate new password
        if (req.getNewPassword() == null || req.getNewPassword().length() < 4) {
            throw new RuntimeException("New password too short");
        }

        // 🔐 update password
        user.setPassword(encoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }
    
    public void changeFundPassword(String username, ChangeFundPasswordRequest req) {

        User user = userRepo.findByUsername(username).orElseThrow();

        if (!encoder.matches(req.getOldFundPassword(), user.getFundPassword())) {
            throw new RuntimeException("Invalid old fund password");
        }

        user.setFundPassword(encoder.encode(req.getNewFundPassword()));
        userRepo.save(user);
    }
    
    public void updateProfile(String username, UpdateProfileRequest req) {

        User user = userRepo.findByUsername(username).orElseThrow();

        if (req.getPhone() != null) {
            user.setPhone(req.getPhone());
        }

        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }

        userRepo.save(user);
    }
    
    public BigDecimal getWithdrawable(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return user.getWithdrawable();
    }
}