package com.game.platform.service;

import com.game.platform.entity.AdminControl;
import com.game.platform.repository.AdminControlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminControlService {

    private final AdminControlRepository repo;

    public AdminControlService(AdminControlRepository repo) {
        this.repo = repo;
    }
    // =========================
    // 🔥 ENABLE PROFIT CONTROL
    // =========================
    @Transactional
    public AdminControl enable(LocalDateTime start,
                               LocalDateTime end,
                               double min,
                               double max) {

        // ✅ TIME VALIDATION
        if (start == null || end == null) {
            throw new RuntimeException("Start and End time required");
        }

        if (start.isAfter(end)) {
            throw new RuntimeException("Start must be before end");
        }

        if (end.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("End time must be in future");
        }

     // ✅ PERCENT VALIDATION (1–20 ONLY)
        if (min < 1 || max < 1) {
            throw new RuntimeException("Minimum percent must be >= 1%");
        }

        if (max > 20) {
            throw new RuntimeException("Maximum percent cannot exceed 20%");
        }

        if (min > max) {
            throw new RuntimeException("Min cannot be greater than max");
        }

        // ✅ DISABLE PREVIOUS CONTROLS
        repo.deactivateAll();

        // ✅ CREATE NEW CONTROL
        AdminControl control = new AdminControl();
        control.setStartTime(start);
        control.setEndTime(end);
        control.setMinPercent(min);
        control.setMaxPercent(max);
        control.setActive(true);

        return repo.save(control);
    }

    // =========================
    // ❌ DISABLE ALL
    // =========================
    @Transactional
    public void disable() {
        repo.deactivateAll();
    }

    // =========================
    // 🧠 CURRENT ACTIVE CONTROL
    // =========================
    public AdminControl getActiveControl() {

        LocalDateTime now =
                LocalDateTime.now(java.time.ZoneOffset.UTC);

        AdminControl control = repo
            .findTopByActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByIdDesc(
                now,
                now
            )
            .orElse(null);

        System.out.println("NOW: " + now);

        // ✅ VERY IMPORTANT NULL CHECK
        if (control == null) {

            System.out.println("⚠️ No active Venice control");

            return null;
        }

        System.out.println("START: " + control.getStartTime());
        System.out.println("END: " + control.getEndTime());

        // 🔥 AUTO CLEAN EXPIRED
        if (now.isAfter(control.getEndTime())) {

            control.setActive(false);

            repo.save(control);

            return null;
        }

        return control;
    }
    
    public double getMultiplier() {
        AdminControl control = getActiveControl();

        if (control != null) {
            return 2.15; // admin active
        }

        return 1.98; // normal
    }
}