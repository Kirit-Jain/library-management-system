package com.library.management.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.management.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    private final ObjectMapper objectMapper;

    @Autowired
    private Map<String, Bucket> bucketCache;

    @Override
    public boolean preHandle(
        HttpServletRequest request,
        HttpServletResponse response,
        Object handler) throws Exception {

        String clientId = getClientIdentifier(request);
        boolean isAuthenticated = isUserAuthenticated();

        // Get or create bucket for this client
        Bucket bucket = bucketCache.computeIfAbsent(clientId,
            k -> rateLimitConfig.createNewBucket(isAuthenticated));

        if (bucket.tryConsume(1)) {
            // Add headers showing remaining requests
            long remaining = bucket.getAvailableTokens();
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
            response.addHeader("X-Rate-Limit-Limit",
                isAuthenticated ? "120" : "60");
            return true;
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for: {}", clientId);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "RATE_LIMIT_EXCEEDED");
            errorResponse.put("message", "Too many requests. Please try again later.");
            errorResponse.put("timestamp", LocalDateTime.now().toString());

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return false;
        }
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get user from security context first
        if (isUserAuthenticated()) {
            return "user:" + SecurityContextHolder.getContext()
                .getAuthentication().getName();
        }

        // Fall back to IP address
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }
        return "ip:" + request.getRemoteAddr();
    }

    private boolean isUserAuthenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
            && auth.isAuthenticated()
            && !"anonymousUser".equals(auth.getPrincipal());
    }
}