package com.game.platform.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.game.platform.entity.Transaction;
import com.game.platform.entity.User;
import com.game.platform.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    // 🔥 GET ALL USERS
    @GetMapping("/users")
    public List<User> users() {
        return service.getAllUsers();
    }

    // 💰 RECHARGE
    @PostMapping("/recharge")
    public String recharge(@RequestParam Long userId,
                           @RequestParam Double amount) {

        service.recharge(userId, amount);
        return "Recharge successful";
    }

    // 📥 PENDING WITHDRAWS
    @GetMapping("/withdraw/pending")
    public List<Transaction> pending() {
        return service.getPendingWithdraws();
    }

    // ✅ APPROVE
    @PostMapping("/withdraw/approve")
    public String approve(@RequestParam Long txId) {
        service.approveWithdraw(txId);
        return "Approved";
    }

    // ❌ REJECT
    @PostMapping("/withdraw/reject")
    public String reject(@RequestParam Long txId) {
        service.rejectWithdraw(txId);
        return "Rejected";
    }
    @GetMapping("/users/search")
    public List<User> search(@RequestParam String keyword) {
        return service.searchUsers(keyword);
    }
}