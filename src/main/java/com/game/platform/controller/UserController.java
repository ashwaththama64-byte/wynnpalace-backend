package com.game.platform.controller;

import java.security.Principal;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.game.platform.dto.*;
import com.game.platform.service.UserService;

@RestController
@RequestMapping("/api/user")
@CrossOrigin("*")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // =========================
    // 📊 DASHBOARD
    // =========================
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Principal principal) {
        try {
            if (principal == null) {
                return ResponseEntity.status(401).body(error("Unauthorized"));
            }

            return ResponseEntity.ok(success(service.getDashboard(principal.getName())));

        } catch (Exception e) {
            e.printStackTrace(); // 🔥 ADD THIS
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // =========================
    // 💰 WITHDRAWABLE AMOUNT (🔥 NEW)
    // =========================
    @GetMapping("/withdrawable")
    public ResponseEntity<?> getWithdrawable(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(error("Unauthorized"));
        }

        try {
            var amount = service.getWithdrawable(principal.getName());
            return ResponseEntity.ok(success(amount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

 // =========================
 // 📄 ALL TRANSACTIONS
 // =========================
 @GetMapping("/transactions")
 public ResponseEntity<?> all(Principal p) {
     if (p == null) {
         return ResponseEntity.status(401).body(error("Unauthorized"));
     }

     try {
         return ResponseEntity.ok(success(service.getAllTransactions(p.getName())));
     } catch (Exception e) {
         e.printStackTrace();
         return ResponseEntity.badRequest().body(error("Failed to load transactions"));
     }
 }

 // =========================
 // 📅 TODAY
 // =========================
 @GetMapping("/transactions/today")
 public ResponseEntity<?> today(Principal p) {
     if (p == null) {
         return ResponseEntity.status(401).body(error("Unauthorized"));
     }

     try {
         return ResponseEntity.ok(success(service.getTodayTransactions(p.getName())));
     } catch (Exception e) {
         e.printStackTrace();
         return ResponseEntity.badRequest().body(error("Failed to load today's transactions"));
     }
 }

 // =========================
 // 📆 WEEK
 // =========================
 @GetMapping("/transactions/week")
 public ResponseEntity<?> week(Principal p) {
     if (p == null) {
         return ResponseEntity.status(401).body(error("Unauthorized"));
     }

     try {
         return ResponseEntity.ok(success(service.getWeekTransactions(p.getName())));
     } catch (Exception e) {
         e.printStackTrace();
         return ResponseEntity.badRequest().body(error("Failed to load week's transactions"));
     }
 }
    // =========================
    // 💸 WITHDRAW
    // =========================
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest req, Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body(error("Unauthorized"));
        }

        try {
            service.requestWithdraw(p.getName(), req);
            return ResponseEntity.ok(success("Withdraw request submitted ✅"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // =========================
    // 👤 PROFILE
    // =========================
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {

        System.out.println("🔥 PRINCIPAL: " + principal);
        System.out.println("🔥 USERNAME: " + (principal != null ? principal.getName() : "NULL"));

        if (principal == null) {
            return ResponseEntity.status(401).body(error("Unauthorized"));
        }

        return ResponseEntity.ok(success(service.getProfile(principal.getName())));
    }

    // =========================
    // 🔐 CHANGE PASSWORD
    // =========================
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest req,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(error("Unauthorized"));
        }

        try {
            service.changePassword(principal.getName(), req);
            return ResponseEntity.ok(success("Password changed successfully ✅"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // =========================
    // 🔐 CHANGE FUND PASSWORD
    // =========================
    @PostMapping("/change-fund-password")
    public ResponseEntity<?> changeFundPassword(
            @RequestBody ChangeFundPasswordRequest req,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(error("Unauthorized"));
        }

        try {
            service.changeFundPassword(principal.getName(), req);
            return ResponseEntity.ok(success("Fund password updated ✅"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    // =========================
    // ✏️ UPDATE PROFILE
    // =========================
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileRequest req,
            Principal principal) {

        if (principal == null) {
            return ResponseEntity.status(401).body(error("Unauthorized"));
        }

        service.updateProfile(principal.getName(), req);

        return ResponseEntity.ok(success("Profile updated ✅"));
    }

    // =========================
    // 🔥 COMMON RESPONSE FORMAT
    // =========================
    private Map<String, Object> success(Object data) {
        return Map.of("success", true, "data", data);
    }

    private Map<String, Object> error(String msg) {
        return Map.of("success", false, "message", msg);
    }
}