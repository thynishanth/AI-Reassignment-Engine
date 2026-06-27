package com.hackathon.backend.web.dto.response;

import com.hackathon.backend.domain.enums.OrderStatus;
import java.time.LocalDateTime;

public record OrderResponse(
        String id,
        String description,
        String assignedAgentId,
        OrderStatus status,
        LocalDateTime createdAt
) {
}
