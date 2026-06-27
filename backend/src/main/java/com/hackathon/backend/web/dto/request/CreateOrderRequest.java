package com.hackathon.backend.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank String id,
        @NotBlank String description,
        @NotBlank String assignedAgentId
) {
}
