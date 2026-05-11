package com.game.platform.repository;

import com.game.platform.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    // 🔐 LOGIN SUPPORT
    Optional<Admin> findByUsername(String username);
}