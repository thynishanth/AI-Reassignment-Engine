package com.hackathon.backend.web.dto.request;

import com.hackathon.backend.domain.enums.RoutingStrategyType;
import jakarta.validation.constraints.NotNull;

public record SwitchRoutingStrategyRequest(
        @NotNull(message = "Strategy type is required")
        RoutingStrategyType strategyType
) {
}
