package com.game.platform.controller;

import com.game.platform.service.AdminControlService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/game")
public class GameConfigController {

    private final AdminControlService service;

    public GameConfigController(AdminControlService service) {
        this.service = service;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {

        double multiplier = service.getMultiplier();

        return Map.of(
            "multiplier", multiplier
        );
    }
}