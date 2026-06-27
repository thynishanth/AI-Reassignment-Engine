package com.hackathon.backend.web.dto.request;

import com.hackathon.backend.domain.enums.AgentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAgentStatusRequest(
        @NotNull AgentStatus status
) {
}
