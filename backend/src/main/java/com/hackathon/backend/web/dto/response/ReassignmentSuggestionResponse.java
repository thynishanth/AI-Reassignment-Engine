package com.hackathon.backend.web.dto.response;

import com.hackathon.backend.domain.enums.ReassignmentSuggestionStatus;
import com.hackathon.backend.domain.enums.TriggerReason;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReassignmentSuggestionResponse(
        String id,
        String orderId,
        String recommendedAgentId,
        BigDecimal confidenceScore,
        String reasoning,
        ReassignmentSuggestionStatus status,
        TriggerReason triggerReason,
        LocalDateTime createdAt
) {
}
