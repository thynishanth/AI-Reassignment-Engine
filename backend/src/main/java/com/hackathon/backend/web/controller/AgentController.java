package com.hackathon.backend.web.controller;

import com.hackathon.backend.domain.enums.AgentStatus;
import com.hackathon.backend.service.AgentService;
import com.hackathon.backend.web.dto.request.UpdateAgentStatusRequest;
import com.hackathon.backend.web.dto.response.AgentResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public List<AgentResponse> listAgents(@RequestParam(required = false) AgentStatus status) {
        return agentService.listAgents(status);
    }

    @PatchMapping("/{id}/status")
    public AgentResponse updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateAgentStatusRequest request
    ) {
        return agentService.updateStatus(id, request);
    }
}
