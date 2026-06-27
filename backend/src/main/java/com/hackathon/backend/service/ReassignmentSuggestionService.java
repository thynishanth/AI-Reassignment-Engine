package com.hackathon.backend.service;

import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.entity.Order;
import com.hackathon.backend.domain.entity.ReassignmentSuggestion;
import com.hackathon.backend.domain.enums.OrderStatus;
import com.hackathon.backend.domain.enums.ReassignmentSuggestionStatus;
import com.hackathon.backend.domain.enums.TriggerReason;
import com.hackathon.backend.domain.repository.AgentRepository;
import com.hackathon.backend.domain.repository.OrderRepository;
import com.hackathon.backend.domain.repository.ReassignmentSuggestionRepository;
import com.hackathon.backend.service.routing.RoutingDecision;
import com.hackathon.backend.service.routing.RoutingStrategyResolver;
import com.hackathon.backend.web.dto.request.UpdateSuggestionStatusRequest;
import com.hackathon.backend.web.dto.response.ReassignmentSuggestionResponse;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReassignmentSuggestionService {

    private final ReassignmentSuggestionRepository suggestionRepository;
    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final RoutingStrategyResolver routingStrategyResolver;

    @Transactional(readOnly = true)
    public Iterable<ReassignmentSuggestionResponse> listPendingSuggestions() {
        return suggestionRepository.findByStatusOrderByCreatedAtDesc(ReassignmentSuggestionStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReassignmentSuggestionResponse updateStatus(String id, UpdateSuggestionStatusRequest request) {
        ReassignmentSuggestion suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Suggestion not found"));

        suggestion.setStatus(request.status());

        if (request.status() == ReassignmentSuggestionStatus.ACCEPTED) {
            applyAcceptance(suggestion);
        }

        return toResponse(suggestionRepository.save(suggestion));
    }

    @Transactional
    public ReassignmentSuggestionResponse createAndPersistSuggestion(
            Order order,
            List<Agent> availableAgents,
            TriggerReason triggerReason
    ) {
        RoutingDecision routingDecision = routingStrategyResolver.resolve()
                .recommend(order, availableAgents, triggerReason);

        ReassignmentSuggestion suggestion = new ReassignmentSuggestion();
        suggestion.setId("SUG-" + order.getId() + "-" + System.nanoTime());
        suggestion.setOrder(order);
        suggestion.setRecommendedAgent(routingDecision.recommendedAgent());
        suggestion.setConfidenceScore(routingDecision.confidenceScore());
        suggestion.setReasoning(routingDecision.reasoning());
        suggestion.setStatus(ReassignmentSuggestionStatus.PENDING);
        suggestion.setTriggerReason(triggerReason);

        order.setStatus(OrderStatus.REASSIGNMENT_PENDING);
        orderRepository.save(order);

        return toResponse(suggestionRepository.save(suggestion));
    }

    private void applyAcceptance(ReassignmentSuggestion suggestion) {
        Order order = suggestion.getOrder();
        Agent previousAgent = order.getAssignedAgent();
        Agent nextAgent = suggestion.getRecommendedAgent();

        if (previousAgent != null && !previousAgent.getId().equals(nextAgent.getId())) {
            previousAgent.setActiveOrderCount(Math.max(0, safeCount(previousAgent.getActiveOrderCount()) - 1));
            agentRepository.save(previousAgent);
        }

        nextAgent.setActiveOrderCount(safeCount(nextAgent.getActiveOrderCount()) + 1);
        agentRepository.save(nextAgent);

        order.setAssignedAgent(nextAgent);
        order.setStatus(OrderStatus.REASSIGNED);
        orderRepository.save(order);
    }

    private ReassignmentSuggestionResponse toResponse(ReassignmentSuggestion suggestion) {
        return new ReassignmentSuggestionResponse(
                suggestion.getId(),
                suggestion.getOrder().getId(),
                suggestion.getRecommendedAgent().getId(),
                suggestion.getConfidenceScore(),
                suggestion.getReasoning(),
                suggestion.getStatus(),
                suggestion.getTriggerReason(),
                suggestion.getCreatedAt()
        );
    }

    private int safeCount(Integer count) {
        return Optional.ofNullable(count).orElse(0);
    }
}
