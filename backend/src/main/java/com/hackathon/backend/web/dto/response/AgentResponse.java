package com.hackathon.backend.web.dto.response;

import com.hackathon.backend.domain.enums.AgentStatus;

public record AgentResponse(
        String id,
        String name,
        Integer activeOrderCount,
        AgentStatus status
) {
}
