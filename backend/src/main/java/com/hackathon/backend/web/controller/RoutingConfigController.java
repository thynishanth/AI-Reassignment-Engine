package com.hackathon.backend.web.controller;

import com.hackathon.backend.domain.enums.RoutingStrategyType;
import com.hackathon.backend.service.routing.RoutingStrategyResolver;
import com.hackathon.backend.service.routing.StrategyFailureTracker;
import com.hackathon.backend.web.dto.request.SwitchRoutingStrategyRequest;
import com.hackathon.backend.web.dto.response.RoutingStrategyStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config/routing")
@RequiredArgsConstructor
public class RoutingConfigController {

    private final RoutingStrategyResolver routingStrategyResolver;
    private final StrategyFailureTracker failureTracker;

    @GetMapping("/strategy")
    public RoutingStrategyStatusResponse getActiveStrategy() {
        RoutingStrategyType activeStrategy = routingStrategyResolver.getActiveStrategyType();
        return new RoutingStrategyStatusResponse(
                activeStrategy,
                "Routing strategy switched to " + activeStrategy + " without server restart",
                getStrategyDescription(activeStrategy)
        );
    }

    @PatchMapping("/strategy")
    public RoutingStrategyStatusResponse switchStrategy(@Valid @RequestBody SwitchRoutingStrategyRequest request) {
        RoutingStrategyType newStrategy = request.strategyType();
        
        try {
            routingStrategyResolver.setActiveStrategyType(newStrategy);
            
            // Reset failure count when manually switching strategy
            if (newStrategy == RoutingStrategyType.AI) {
                failureTracker.resetFailureCount();
            }
            
            return new RoutingStrategyStatusResponse(
                    newStrategy,
                    "Successfully switched to " + newStrategy,
                    getStrategyDescription(newStrategy)
            );
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    ex.getMessage()
            );
        }
    }

    @GetMapping("/health")
    public StrategyFailureTracker.StrategyHealthStatus getStrategyHealth() {
        return failureTracker.getHealthStatus();
    }

    @PostMapping("/fallback")
    public RoutingStrategyStatusResponse manualFallback() {
        routingStrategyResolver.setActiveStrategyType(RoutingStrategyType.RULE_BASED);
        failureTracker.resetFailureCount();
        
        return new RoutingStrategyStatusResponse(
                RoutingStrategyType.RULE_BASED,
                "Manually fallen back to RULE_BASED strategy due to LLM issues",
                getStrategyDescription(RoutingStrategyType.RULE_BASED)
        );
    }

    private String getStrategyDescription(RoutingStrategyType strategyType) {
        return switch (strategyType) {
            case RULE_BASED -> "Rule-based routing: Prioritizes agent workload and zone affinity using business rules. Fast and predictable.";
            case AI -> "AI-powered routing: Uses Gemini LLM to intelligently assign orders based on context. Slower but more intelligent.";
            case ZONE_AFFINITY -> "Zone affinity routing: Prioritizes agents in the same pickup/dropoff zone to minimize travel distance.";
        };
    }
}
