package com.game.platform.repository;

import com.game.platform.entity.AdminControl;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdminControlRepository extends JpaRepository<AdminControl, Long> {

    // =========================
    // ✅ ACTIVE CONTROL (INCLUSIVE TIME)
    // =========================
    Optional<AdminControl> findTopByActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByIdDesc(
            LocalDateTime now1,
            LocalDateTime now2
    );

    // =========================
    // 🔥 BULK DEACTIVATE (IMPORTANT)
    // =========================
    @Modifying
    @Query("UPDATE AdminControl a SET a.active = false")
    void deactivateAll();
}