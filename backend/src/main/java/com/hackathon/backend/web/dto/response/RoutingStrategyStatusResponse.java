package com.hackathon.backend.web.dto.response;

import com.hackathon.backend.domain.enums.RoutingStrategyType;

public record RoutingStrategyStatusResponse(
        RoutingStrategyType activeStrategy,
        String message,
        String description
) {
}
