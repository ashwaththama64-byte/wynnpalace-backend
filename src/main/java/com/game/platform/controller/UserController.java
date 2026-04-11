package com.game.platform.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.game.platform.dto.DashboardResponse;
import com.game.platform.dto.WithdrawRequest;
import com.game.platform.entity.Transaction;
import com.game.platform.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // 📊 DASHBOARD
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard(Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(service.getDashboard(principal.getName()));
    }

    // 📄 ALL TRANSACTIONS
    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> all(Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(service.getAllTransactions(p.getName()));
    }

    // 📅 TODAY
    @GetMapping("/transactions/today")
    public ResponseEntity<List<Transaction>> today(Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(service.getTodayTransactions(p.getName()));
    }

    // 📆 WEEK
    @GetMapping("/transactions/week")
    public ResponseEntity<List<Transaction>> week(Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(service.getWeekTransactions(p.getName()));
    }

    // 💸 WITHDRAW
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest req, Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (req.getAmount() == null || req.getAmount().doubleValue() <= 0) {
            return ResponseEntity.badRequest().body("Invalid amount");
        }

        service.requestWithdraw(p.getName(), req);
        return ResponseEntity.ok("Withdraw request submitted ✅");
    }
}