package com.game.platform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static class RequestInfo {
        int count;
        long timestamp;

        RequestInfo(int count, long timestamp) {
            this.count = count;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    private static final int MAX_REQUESTS = 100; // 🔥 increased
    private static final long TIME_WINDOW = 60_000; // 1 minute

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // =========================
        // 🔥 SKIP RATE LIMIT (IMPORTANT)
        // =========================
        if (path.startsWith("/chat") ||              // WebSocket
            path.startsWith("/api/chat") ||          // Chat APIs
            path.startsWith("/ws") ||                // fallback
            
            // ✅ ADMIN SAFE
            path.startsWith("/api/admin") ||

            // 🔥 ADD THESE (CRITICAL)
            path.startsWith("/api/results") ||
            path.startsWith("/api/game") ||
            path.startsWith("/api/bets")
        ) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();

        RequestInfo info = requestCounts.get(ip);

        if (info == null) {
            requestCounts.put(ip, new RequestInfo(1, now));
        } else {

            if (now - info.timestamp > TIME_WINDOW) {
                info.count = 1;
                info.timestamp = now;
            } else {
                info.count++;
            }

            if (info.count > MAX_REQUESTS) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write("{\"message\": \"Too many requests. Try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}