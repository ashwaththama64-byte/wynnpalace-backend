package com.game.platform.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.game.platform.dto.AddCardRequest;
import com.game.platform.entity.BankCard;
import com.game.platform.service.BankService;

@RestController
@RequestMapping("/api/user/cards")
@CrossOrigin("*")
public class BankController {

    private final BankService service;

    public BankController(BankService service) {
        this.service = service;
    }

    // ➕ ADD CARD
    @PostMapping
    public ResponseEntity<?> addCard(@RequestBody AddCardRequest req, Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        if (req.getAccountNumber() == null || req.getBankName() == null) {
            return ResponseEntity.badRequest().body("Invalid card details");
        }

        service.addCard(p.getName(), req);
        return ResponseEntity.ok("Card added ✅");
    }

    // 📄 GET CARDS
    @GetMapping
    public ResponseEntity<List<BankCard>> getCards(Principal p) {

        if (p == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(service.getCards(p.getName()));
    }
}