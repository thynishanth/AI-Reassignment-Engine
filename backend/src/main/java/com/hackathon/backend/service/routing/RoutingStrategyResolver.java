package com.hackathon.backend.service.routing;

import com.hackathon.backend.domain.enums.RoutingStrategyType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RoutingStrategyResolver {

    private final Map<RoutingStrategyType, RoutingStrategy> strategies;
    private volatile RoutingStrategyType activeStrategyType;
    private final StrategyFailureTracker failureTracker;

    public RoutingStrategyResolver(
            List<RoutingStrategy> strategies,
            @Value("${routing.strategy:RULE_BASED}") RoutingStrategyType activeStrategyType,
            StrategyFailureTracker failureTracker
    ) {
        this.strategies = new EnumMap<>(RoutingStrategyType.class);
        for (RoutingStrategy strategy : strategies) {
            this.strategies.put(strategy.type(), strategy);
        }
        this.activeStrategyType = activeStrategyType;
        this.failureTracker = failureTracker;
    }

    public RoutingStrategy resolve() {
        RoutingStrategyType strategyToUse = activeStrategyType;
        
        // Auto-fallback if AI has too many failures
        if (strategyToUse == RoutingStrategyType.AI && failureTracker.shouldAutoFallback()) {
            strategyToUse = RoutingStrategyType.RULE_BASED;
        }
        
        RoutingStrategy strategy = strategies.get(strategyToUse);
        if (strategy == null) {
            throw new IllegalStateException("No routing strategy registered for " + strategyToUse);
        }
        return strategy;
    }

    public RoutingStrategyType getActiveStrategyType() {
        return activeStrategyType;
    }

    public void setActiveStrategyType(RoutingStrategyType strategyType) {
        if (!strategies.containsKey(strategyType)) {
            throw new IllegalArgumentException("No routing strategy registered for " + strategyType);
        }
        this.activeStrategyType = strategyType;
    }
}
