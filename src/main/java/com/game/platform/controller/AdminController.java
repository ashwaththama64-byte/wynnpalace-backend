package com.game.platform.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.game.platform.dto.UserResponse;
import com.game.platform.dto.RechargeRequest;
import com.game.platform.dto.WithdrawAdminResponse;
import com.game.platform.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin("*")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    // ✅ USERS
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> users() {
        return ResponseEntity.ok(service.getAllUsers());
    }

    // ✅ RECHARGE
    @PostMapping("/recharge")
    public ResponseEntity<?> recharge(@RequestBody RechargeRequest req) {

        if (req.getUserId() == null || req.getAmount() == null) {
            return ResponseEntity.badRequest().body("Invalid request");
        }

        service.recharge(req.getUserId(), req.getAmount());
        return ResponseEntity.ok("Recharge successful ✅");
    }

    // 📥 PENDING WITHDRAWS
    @GetMapping("/withdraw/pending")
    public ResponseEntity<List<WithdrawAdminResponse>> pending() {
        return ResponseEntity.ok(service.getPendingWithdraws());
    }

    // ✅ APPROVE
    @PostMapping("/withdraw/approve")
    public ResponseEntity<?> approve(@RequestParam Long txId) {
        service.approveWithdraw(txId);
        return ResponseEntity.ok("Approved ✅");
    }

    // ❌ REJECT
    @PostMapping("/withdraw/reject")
    public ResponseEntity<?> reject(@RequestParam Long txId) {
        service.rejectWithdraw(txId);
        return ResponseEntity.ok("Rejected ❌");
    }

    // 🔍 SEARCH
    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(service.searchUsers(keyword));
    }
}