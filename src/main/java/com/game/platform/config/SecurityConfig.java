package com.game.platform.config;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.game.platform.security.JwtFilter;
import com.game.platform.security.RateLimitFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(JwtFilter jwtFilter, RateLimitFilter rateLimitFilter) {
        this.jwtFilter = jwtFilter;
        this.rateLimitFilter = rateLimitFilter;
    }

    // 🔐 Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔐 Auth Manager (if needed for login)
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 🔥 MAIN SECURITY CONFIG
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // =========================
                // 🔓 PUBLIC APIs
                // =========================
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/auth/**").permitAll()
                .requestMatchers("/chat/**").permitAll()
                .requestMatchers("/api/chat/**").permitAll()
                .requestMatchers("/api/upload").permitAll()
                .requestMatchers("/uploads/**").permitAll()

                // 🎯 GAME RESULTS (PUBLIC)
                .requestMatchers("/api/results/**").permitAll()

                // =========================
                // 🎮 GAME (USER + ADMIN)
                // =========================
                .requestMatchers("/api/game/config").permitAll()
                .requestMatchers("/api/game/**")
                .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                // =========================
                // 👤 USER BETS
                // =========================
                .requestMatchers("/api/bets/**")
                .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                // =========================
                // 🔧 ADMIN CONTROL ONLY
                // =========================
                .requestMatchers("/api/admin/control/**")
                .hasAuthority("ROLE_ADMIN")

                // =========================
                // 🔧 ALL ADMIN APIs
                // =========================
                .requestMatchers("/api/admin/**")
                .hasAuthority("ROLE_ADMIN")

                // =========================
                // 🔒 EVERYTHING ELSE
                // =========================
                .anyRequest().authenticated()
            )

            // 🔥 FILTER ORDER
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}