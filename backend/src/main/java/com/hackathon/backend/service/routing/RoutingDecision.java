package com.hackathon.backend.service.routing;

import com.hackathon.backend.domain.entity.Agent;
import java.math.BigDecimal;

public record RoutingDecision(
        Agent recommendedAgent,
        BigDecimal confidenceScore,
        String reasoning
) {
}
