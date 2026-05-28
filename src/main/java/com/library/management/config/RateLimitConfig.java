package com.library.management.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Configuration
public class RateLimitConfig {

    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${rate-limit.requests-per-minute-auth:120}")
    private int requestsPerMinuteAuth;

    // Stores buckets per IP address
    @Bean
    public Map<String, Bucket> bucketCache() {
        return new ConcurrentHashMap<>();
    }

    public Bucket createNewBucket(boolean authenticated) {
        int limit = authenticated ? requestsPerMinuteAuth : requestsPerMinute;
        Bandwidth bandwidth = Bandwidth.classic(
            limit,
            Refill.intervally(limit, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(bandwidth).build();
    }
}