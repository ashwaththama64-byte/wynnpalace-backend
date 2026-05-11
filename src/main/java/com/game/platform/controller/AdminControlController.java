package com.game.platform.controller;

import com.game.platform.dto.AdminControlRequest;
import com.game.platform.entity.AdminControl;
import com.game.platform.service.AdminControlService;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/control")
public class AdminControlController {

    private final AdminControlService service;

    public AdminControlController(AdminControlService service) {
        this.service = service;
    }
    

    // =========================
    // 🔥 ENABLE ADMIN CONTROL
    // =========================
    @PostMapping("/enable")
    public Map<String, Object> enable(@RequestBody AdminControlRequest req) {

        Map<String, Object> res = new java.util.HashMap<>();

        try {

            AdminControl control = service.enable(
                    req.getStartTime(),
                    req.getEndTime(),
                    req.getMinPercent(),
                    req.getMaxPercent()
            );

            res.put("success", true);
            res.put("message", "Admin control enabled");
            res.put("data", control);

        } catch (Exception e) {

            res.put("success", false);
            res.put("message", e.getMessage());
        }

        return res;
    }
    // =========================
    // ❌ DISABLE ADMIN CONTROL
    // =========================
    @DeleteMapping
    public Map<String, Object> disable() {

        service.disable();

        Map<String, Object> res = new java.util.HashMap<>();
        res.put("success", true);
        res.put("message", "Admin control disabled");

        return res;
    }

    // =========================
    // 🔍 GET STATUS
    // =========================
    @GetMapping("/status")
    public Map<String, Object> status() {

        AdminControl control = service.getActiveControl();

        Map<String, Object> res = new java.util.HashMap<>();

        res.put("success", true);
        res.put("active", control != null);
        res.put("data", control); // ✅ SAFE (can be null)

        return res;
    }
}