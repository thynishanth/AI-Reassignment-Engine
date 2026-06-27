package com.hackathon.backend.service;

import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.entity.Order;
import com.hackathon.backend.domain.enums.AgentStatus;
import com.hackathon.backend.domain.enums.OrderStatus;
import com.hackathon.backend.domain.enums.ReassignmentSuggestionStatus;
import com.hackathon.backend.domain.enums.TriggerReason;
import com.hackathon.backend.domain.event.AgentWentOfflineEvent;
import com.hackathon.backend.domain.repository.AgentRepository;
import com.hackathon.backend.domain.repository.OrderRepository;
import com.hackathon.backend.domain.repository.ReassignmentSuggestionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class AgentOfflineReplanListener {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final ReassignmentSuggestionRepository suggestionRepository;
    private final ReassignmentSuggestionService suggestionService;

    @TransactionalEventListener
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAgentWentOffline(AgentWentOfflineEvent event) {
        Agent offlineAgent = event.getAgent();
        log.info("Agentic re-planning triggered for agent going offline: {}", offlineAgent.getId());

        List<Order> affectedOrders = orderRepository.findByAssignedAgent_IdAndStatus(
                offlineAgent.getId(),
                OrderStatus.ASSIGNED
        );

        log.info("Found {} orders assigned to offline agent {}", affectedOrders.size(), offlineAgent.getId());

        for (Order order : affectedOrders) {
            replanOrderIfNeeded(order, offlineAgent);
        }
    }

    private void replanOrderIfNeeded(Order order, Agent offlineAgent) {
        boolean alreadyPending = suggestionRepository
                .findFirstByOrder_IdAndStatusAndTriggerReason(
                        order.getId(),
                        ReassignmentSuggestionStatus.PENDING,
                        TriggerReason.AGENT_OFFLINE
                )
                .isPresent();

        if (alreadyPending) {
            log.debug("Skipping re-plan for order {} - pending AGENT_OFFLINE suggestion already exists", order.getId());
            return;
        }

        try {
            List<Agent> availableAgents = agentRepository.findByStatus(AgentStatus.AVAILABLE);

            if (availableAgents.isEmpty()) {
                log.warn("No available agents for re-planning order {}", order.getId());
                return;
            }

            suggestionService.createAndPersistSuggestion(order, availableAgents, TriggerReason.AGENT_OFFLINE);
            log.info("Created re-plan suggestion for order {} due to agent {} going offline",
                    order.getId(), offlineAgent.getId());

        } catch (Exception ex) {
            log.error("Failed to generate re-plan suggestion for order {}: {}", order.getId(), ex.getMessage(), ex);
        }
    }
}
