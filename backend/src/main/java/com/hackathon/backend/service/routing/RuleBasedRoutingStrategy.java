package com.hackathon.backend.service.routing;

import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.entity.Order;
import com.hackathon.backend.domain.enums.RoutingStrategyType;
import com.hackathon.backend.domain.enums.TriggerReason;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RuleBasedRoutingStrategy implements RoutingStrategy {

    @Override
    public RoutingStrategyType type() {
        return RoutingStrategyType.RULE_BASED;
    }

    @Override
    public RoutingDecision recommend(Order order, List<Agent> candidateAgents, TriggerReason triggerReason) {
        Agent recommendedAgent = candidateAgents.stream()
                .filter(agent -> order.getAssignedAgent() == null || !agent.getId().equals(order.getAssignedAgent().getId()))
                .min(Comparator
                        .comparingInt((Agent agent) -> safeCount(agent.getActiveOrderCount()))
                        .thenComparing(Agent::getId))
                .orElseGet(() -> order.getAssignedAgent());

        if (recommendedAgent == null) {
            throw new IllegalStateException("No candidate agents available for routing");
        }

        return new RoutingDecision(
                recommendedAgent,
                BigDecimal.valueOf(0.75),
                "Recommended " + recommendedAgent.getName() + " because they currently have the lowest active load."
        );
    }

    private int safeCount(Integer count) {
        return Optional.ofNullable(count).orElse(0);
    }
}
