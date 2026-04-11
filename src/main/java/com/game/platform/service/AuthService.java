package com.game.platform.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.game.platform.dto.AuthResponse;
import com.game.platform.dto.LoginRequest;
import com.game.platform.dto.RegisterRequest;
import com.game.platform.entity.RefreshToken;
import com.game.platform.entity.User;
import com.game.platform.repository.RefreshTokenRepository;
import com.game.platform.repository.UserRepository;
import com.game.platform.util.JwtUtil;

@Service
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshRepo;

    public AuthService(UserRepository repo,
                       PasswordEncoder encoder,
                       JwtUtil jwtUtil,
                       RefreshTokenRepository refreshRepo) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.refreshRepo = refreshRepo;
    }

    // =========================
    // 🔥 GENERATE USER CODE
    // =========================
    private String generateUserCode() {
        String code;
        do {
            code = String.valueOf(1000000 + new Random().nextInt(900000));
        } while (repo.existsByUserCode(code));
        return code;
    }

    // =========================
    // ✅ REGISTER
    // =========================
    public void register(RegisterRequest req) {

        if (repo.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (req.getFundPassword() == null || req.getFundPassword().isEmpty()) {
            throw new RuntimeException("Fund password is required");
        }

        User user = new User();

        user.setUsername(req.getUsername());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setFundPassword(encoder.encode(req.getFundPassword()));
        user.setUserCode(generateUserCode());
        user.setBalance(BigDecimal.ZERO);

        repo.save(user);
    }

    // =========================
    // ✅ LOGIN
    // =========================
    public AuthResponse login(LoginRequest req) {

        System.out.println("🔥 LOGIN START");

        User user = repo.findByUsername(req.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String jwtToken = jwtUtil.generateUserToken(user.getUsername());

        System.out.println("🚀 RETURNING RESPONSE");

        // ❌ REMOVE refresh token completely
        return new AuthResponse(
                jwtToken,
                null,
                "USER"
        );
    }

    // =========================
    // 🔄 REFRESH TOKEN
    // =========================
    public AuthResponse refreshToken(String refreshToken) {

        RefreshToken token = refreshRepo.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        User user = repo.findByUsername(token.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String jwtToken = jwtUtil.generateUserToken(user.getUsername());

       // String newJwt = jwtUtil.generateUserToken(user.getUsername());

        return new AuthResponse(
                jwtToken,
                null,
                "USER"
        );
    }

    // =========================
    // 🔄 GENERATE REFRESH TOKEN
    // =========================
    public String generateRefreshToken(String username) {
    	
    	refreshRepo.deleteByUsername(username);

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsername(username);
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        refreshRepo.save(token);

        return token.getToken();
    }

    // =========================
    // 🔐 VERIFY FUND PASSWORD
    // =========================
    public void verifyFundPassword(String username, String fundPassword) {

        User user = repo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getFundPassword() == null ||
            fundPassword == null ||
            !encoder.matches(fundPassword, user.getFundPassword())) {
            throw new RuntimeException("Invalid fund password");
        }
    }
}