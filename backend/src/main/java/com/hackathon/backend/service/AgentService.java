package com.hackathon.backend.service;

import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.enums.AgentStatus;
import com.hackathon.backend.domain.event.AgentWentOfflineEvent;
import com.hackathon.backend.domain.repository.AgentRepository;
import com.hackathon.backend.web.dto.request.UpdateAgentStatusRequest;
import com.hackathon.backend.web.dto.response.AgentResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<AgentResponse> listAgents(AgentStatus status) {
        return (status == null ? agentRepository.findAll() : agentRepository.findByStatus(status))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AgentResponse updateStatus(String id, UpdateAgentStatusRequest request) {
        Agent agent = agentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agent not found"));

        AgentStatus oldStatus = agent.getStatus();
        agent.setStatus(request.status());
        Agent savedAgent = agentRepository.save(agent);

        if (request.status() == AgentStatus.OFFLINE && oldStatus != AgentStatus.OFFLINE) {
            eventPublisher.publishEvent(new AgentWentOfflineEvent(this, savedAgent));
        }

        return toResponse(savedAgent);
    }

    private AgentResponse toResponse(Agent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getName(),
                agent.getActiveOrderCount(),
                agent.getStatus()
        );
    }
}
