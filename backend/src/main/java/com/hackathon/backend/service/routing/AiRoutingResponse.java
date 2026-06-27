package com.hackathon.backend.service.routing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiRoutingResponse(
        String agentId,
        BigDecimal confidence,
        String reasoning
) {
}
