package com.hackathon.backend.web.controller;

import com.hackathon.backend.service.ReassignmentSuggestionService;
import com.hackathon.backend.web.dto.request.UpdateSuggestionStatusRequest;
import com.hackathon.backend.web.dto.response.ReassignmentSuggestionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/suggestions")
@RequiredArgsConstructor
public class ReassignmentSuggestionController {

    private final ReassignmentSuggestionService suggestionService;

    @PatchMapping("/{id}")
    public ReassignmentSuggestionResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateSuggestionStatusRequest request
    ) {
        return suggestionService.updateStatus(id, request);
    }

    @GetMapping
    public Iterable<ReassignmentSuggestionResponse> listPendingSuggestions() {
        return suggestionService.listPendingSuggestions();
    }
}
