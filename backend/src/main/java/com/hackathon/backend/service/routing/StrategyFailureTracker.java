package com.hackathon.backend.service.routing;

import com.hackathon.backend.domain.enums.RoutingStrategyType;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Getter
public class StrategyFailureTracker {

    private final AtomicInteger aiFailureCount = new AtomicInteger(0);
    private final AtomicInteger aiSuccessCount = new AtomicInteger(0);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicLong lastSuccessTime = new AtomicLong(0);

    private static final int FAILURE_THRESHOLD = 5;
    private static final long RECOVERY_WINDOW_MS = 60_000; // 1 minute

    public void recordAiSuccess() {
        aiSuccessCount.incrementAndGet();
        lastSuccessTime.set(System.currentTimeMillis());
        
        // Reset failures on success (recovery)
        if (shouldResetFailures()) {
            aiFailureCount.set(0);
            log.info("AI strategy recovered. Failure count reset.");
        }
    }

    public void recordAiFailure() {
        int failCount = aiFailureCount.incrementAndGet();
        lastFailureTime.set(System.currentTimeMillis());
        
        log.warn("AI strategy failure recorded. Total failures: {}/{}", 
                 failCount, FAILURE_THRESHOLD);
        
        if (failCount >= FAILURE_THRESHOLD) {
            log.error("AI strategy reached failure threshold! Auto-fallback recommended.");
        }
    }

    public boolean shouldAutoFallback() {
        return aiFailureCount.get() >= FAILURE_THRESHOLD;
    }

    public void resetFailureCount() {
        aiFailureCount.set(0);
        log.info("Failure count manually reset.");
    }

    private boolean shouldResetFailures() {
        long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
        return timeSinceLastFailure > RECOVERY_WINDOW_MS;
    }

    public StrategyHealthStatus getHealthStatus() {
        int failures = aiFailureCount.get();
        int successes = aiSuccessCount.get();
        boolean isHealthy = !shouldAutoFallback();
        
        return new StrategyHealthStatus(
                isHealthy ? "HEALTHY" : "DEGRADED",
                failures,
                successes,
                FAILURE_THRESHOLD,
                failures >= FAILURE_THRESHOLD ? "Should fallback to RULE_BASED" : "Operating normally",
                new java.util.Date(lastFailureTime.get()),
                new java.util.Date(lastSuccessTime.get())
        );
    }

    public record StrategyHealthStatus(
            String status,
            int failureCount,
            int successCount,
            int failureThreshold,
            String recommendation,
            java.util.Date lastFailureTime,
            java.util.Date lastSuccessTime
    ) {}
}
