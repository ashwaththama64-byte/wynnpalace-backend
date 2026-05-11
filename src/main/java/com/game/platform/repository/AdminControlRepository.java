package com.game.platform.repository;

import com.game.platform.entity.AdminControl;
import org.springframework.data.jpa.repository.*;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface AdminControlRepository extends JpaRepository<AdminControl, Long> {

    Optional<AdminControl> findTopByActiveTrueAndStartTimeLessThanEqualAndEndTimeGreaterThanEqualOrderByIdDesc(
            OffsetDateTime now1,
            OffsetDateTime now2
    );

    @Modifying
    @Query("UPDATE AdminControl a SET a.active = false")
    void deactivateAll();
}