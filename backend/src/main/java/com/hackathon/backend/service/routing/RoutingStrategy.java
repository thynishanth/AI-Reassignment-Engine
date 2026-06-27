package com.hackathon.backend.service.routing;

import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.entity.Order;
import com.hackathon.backend.domain.enums.RoutingStrategyType;
import com.hackathon.backend.domain.enums.TriggerReason;
import java.util.List;

public interface RoutingStrategy {

    RoutingStrategyType type();

    RoutingDecision recommend(Order order, List<Agent> candidateAgents, TriggerReason triggerReason);
}
