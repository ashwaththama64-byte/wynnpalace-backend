package com.game.platform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.game.platform.entity.GameRound;
import com.game.platform.entity.User;

import jakarta.persistence.LockModeType;

public interface UserRepository extends JpaRepository<User, Long> {
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<User> findByUsername(String username);

    boolean existsByUserCode(String userCode);

    List<User> findByUsernameContainingIgnoreCase(String keyword);

    @Query("""
        SELECT u FROM User u 
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(u.userCode) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<User> searchByUsernameOrUserCode(@Param("keyword") String keyword);

    // 🔥 VERY IMPORTANT (prevents double spending)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameForUpdate(String username);
}