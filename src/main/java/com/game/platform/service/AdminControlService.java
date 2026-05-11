package com.game.platform.service;

import com.game.platform.entity.AdminControl;
import com.game.platform.repository.AdminControlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class AdminControlService {

    private final AdminControlRepository repo;

    public AdminControlService(AdminControlRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public AdminControl enable(OffsetDateTime start,
                               OffsetDateTime end,
                               double min,
                               double max) {

        if (start == null || end == null) {
            throw new RuntimeException("Start and End time required");
        }

        if (start.isAfter(end)) {
            throw new RuntimeException("Start must be before end");
        }

        if (end.isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new RuntimeException("End time must be in future");
        }

        if (min < 1 || max < 1) {
            throw new RuntimeException("Minimum percent must be >= 1%");
        }

        if (max > 20) {
            throw new RuntimeException("Maximum percent cannot exceed 20%");
        }

        if (min > max) {
            throw new RuntimeException("Min cannot be greater than max");
        }

        repo.deactivateAll();

        AdminControl control = new AdminControl();
        control.setStartTime(start);
        control.setEndTime(end);
        control.setMinPercent(min);
        control.setMaxPercent(max);
        control.setActive(true);

        return repo.save(control);
    }

    @Transactional
    public void disable() {
        repo.deactivateAll();
    }

    public AdminControl getActiveControl() {

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        AdminControl control = repo
            .findTopByActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByIdDesc(
                now,
                now
            )
            .orElse(null);

        if (control != null && now.isAfter(control.getEndTime())) {
            control.setActive(false);
            repo.save(control);
            return null;
        }

        return control;
    }

    public double getMultiplier() {
        AdminControl control = getActiveControl();
        return control != null ? 2.15 : 1.98;
    }
}