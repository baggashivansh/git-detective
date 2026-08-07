package com.gitdetective.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lightweight in-memory rate limiter for expensive endpoints. Suitable for single-instance
 * deployments; replace with Redis-backed limiter for multi-instance production clusters.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Deque<Long>> windows = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long windowMs;

    public RateLimitFilter(
            @Value("${gitdetective.security.rate-limit.max-requests:60}") int maxRequests,
            @Value("${gitdetective.security.rate-limit.window-seconds:60}") int windowSeconds) {
        this.maxRequests = Math.max(1, maxRequests);
        this.windowMs = Math.max(1, windowSeconds) * 1000L;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method)) {
            return true;
        }
        return !(path.startsWith("/repositories/analyze")
                || path.startsWith("/investigations")
                || path.startsWith("/assistant/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = clientKey(request) + '|' + request.getRequestURI();
        long now = Instant.now().toEpochMilli();
        Deque<Long> timestamps = windows.computeIfAbsent(key, ignored -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMs) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maxRequests) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setHeader("Retry-After", String.valueOf(windowMs / 1000));
                response.getWriter()
                        .write(
                                "{\"success\":false,\"message\":\"Rate limit exceeded. Retry later.\","
                                        + "\"errorCode\":\"RATE_LIMITED\",\"timestamp\":\""
                                        + Instant.now()
                                        + "\"}");
                return;
            }
            timestamps.addLast(now);
        }

        filterChain.doFilter(request, response);
    }

    private static String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr();
    }
}
