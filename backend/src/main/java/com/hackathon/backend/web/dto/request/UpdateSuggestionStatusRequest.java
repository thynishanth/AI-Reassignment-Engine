package com.hackathon.backend.web.dto.request;

import com.hackathon.backend.domain.enums.ReassignmentSuggestionStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSuggestionStatusRequest(
        @NotNull ReassignmentSuggestionStatus status
) {
}
