package com.game.platform.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

	// =========================
	// 🔐 SIGN KEY
	// =========================
	private Key getSignKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	// =========================
	// 👤 USER TOKEN
	// =========================
	public String generateUserToken(String username) {
		return buildToken(username, "ROLE_USER");
	}

	// =========================
	// 👑 ADMIN TOKEN
	// =========================
	public String generateAdminToken(String username) {
		return buildToken(username, "ROLE_ADMIN");
	}

	// =========================
	// 🔧 COMMON TOKEN BUILDER
	// =========================
	private String buildToken(String username, String role) {
		System.out.println("GENERATING TOKEN ROLE: " + role);
		return Jwts.builder().setSubject(username).claim("role", role) // ✅ FIXED (IMPORTANT)
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(getSignKey(), SignatureAlgorithm.HS256).compact();

	}

	// =========================
	// 📌 EXTRACT USERNAME
	// =========================
	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	// =========================
	// 📌 EXTRACT ROLE (🔥 KEY)
	// =========================
	public String extractRole(String token) {
		return extractAllClaims(token).get("role", String.class);
	}

	// =========================
	// 📌 ALL CLAIMS
	// =========================
	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
	}

	// =========================
	// ✅ VALIDATION (IMPROVED)
	// =========================
	public boolean isTokenValid(String token, String username) {
		try {
			final String extractedUsername = extractUsername(token);
			return (extractedUsername.equals(username) && !isTokenExpired(token));
		} catch (Exception e) {
			return false; // 🔥 safe fallback
		}
	}
	// =========================
	// ⏰ CHECK EXPIRY
	// =========================
	private boolean isTokenExpired(String token) {
		return extractAllClaims(token).getExpiration().before(new Date());
	}
}