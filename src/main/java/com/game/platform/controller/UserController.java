package com.game.platform.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.game.platform.entity.Transaction;
import com.game.platform.entity.User;
import com.game.platform.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public User dashboard(Principal principal) {
        return service.getDashboard(principal.getName());
    }

    @GetMapping("/transactions")
    public List<Transaction> all(Principal p) {
        return service.getAllTransactions(p.getName());
    }

    @GetMapping("/transactions/today")
    public List<Transaction> today(Principal p) {
        return service.getTodayTransactions(p.getName());
    }

    @GetMapping("/transactions/week")
    public List<Transaction> week(Principal p) {
        return service.getWeekTransactions(p.getName());
    }

    @PostMapping("/withdraw")
    public String withdraw(Principal p,
                           @RequestParam Double amount,
                           @RequestParam String fundPassword) {

        service.requestWithdraw(p.getName(), amount, fundPassword);
        return "Withdraw request submitted";
    }
}