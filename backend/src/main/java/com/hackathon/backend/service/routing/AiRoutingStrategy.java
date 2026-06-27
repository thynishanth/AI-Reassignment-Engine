package com.hackathon.backend.service.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.entity.Order;
import com.hackathon.backend.domain.enums.RoutingStrategyType;
import com.hackathon.backend.domain.enums.TriggerReason;
import com.hackathon.backend.infrastructure.llm.LLMGateway;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiRoutingStrategy implements RoutingStrategy {

    private final LLMGateway llmGateway;
    private final ObjectMapper objectMapper;
    private final RuleBasedRoutingStrategy ruleBasedRoutingStrategy;
    private final StrategyFailureTracker failureTracker;

    @Override
    public RoutingStrategyType type() {
        return RoutingStrategyType.AI;
    }

    @Override
    public RoutingDecision recommend(Order order, List<Agent> candidateAgents, TriggerReason triggerReason) {
        try {
            String prompt = buildPrompt(order, candidateAgents, triggerReason);
            AiRoutingResponse response = objectMapper.readValue(llmGateway.callLLM(prompt), AiRoutingResponse.class);
            Agent recommendedAgent = resolveCandidate(response.agentId(), candidateAgents);

            if (recommendedAgent == null || !isValidConfidence(response.confidence())) {
                log.warn("AI routing returned invalid agent or confidence for order {}", order.getId());
                failureTracker.recordAiFailure();
                return fallback(order, candidateAgents, triggerReason);
            }

            failureTracker.recordAiSuccess();
            return new RoutingDecision(
                    recommendedAgent,
                    response.confidence(),
                    Optional.ofNullable(response.reasoning()).filter(text -> !text.isBlank())
                            .orElse("AI recommended " + recommendedAgent.getName() + ".")
            );
        } catch (RestClientException | IllegalStateException | IllegalArgumentException | com.fasterxml.jackson.core.JsonProcessingException ex) {
            log.warn("AI routing failed for order {}: {}", order.getId(), ex.getMessage());
            failureTracker.recordAiFailure();
            return fallback(order, candidateAgents, triggerReason);
        }
    }

    private RoutingDecision fallback(Order order, List<Agent> candidateAgents, TriggerReason triggerReason) {
        return ruleBasedRoutingStrategy.recommend(order, candidateAgents, triggerReason);
    }

    private Agent resolveCandidate(String agentId, List<Agent> candidateAgents) {
        return candidateAgents.stream()
                .filter(agent -> agent.getId().equals(agentId))
                .findFirst()
                .orElse(null);
    }

    private boolean isValidConfidence(BigDecimal confidence) {
        return confidence != null
                && confidence.compareTo(BigDecimal.ZERO) >= 0
                && confidence.compareTo(BigDecimal.ONE) <= 0;
    }

    private String buildPrompt(Order order, List<Agent> candidateAgents, TriggerReason triggerReason) {
        StringBuilder prompt = new StringBuilder();

        if (triggerReason == TriggerReason.AGENT_OFFLINE) {
            prompt.append("EMERGENCY: Re-plan assignment due to agent unavailability.\n");
            prompt.append("An agent has gone offline and their orders need immediate reassignment.\n");
            prompt.append("Current situation: order ").append(order.getId())
                    .append(" was assigned to ").append(order.getAssignedAgent().getId())
                    .append(" but that agent is now OFFLINE.\n");
            prompt.append("Your task: recommend an available agent to take over this order immediately.\n\n");
        } else {
            prompt.append("You are an order routing assistant performing initial assignment.\n");
        }

        prompt.append("Order: {")
                .append("\"id\":\"").append(order.getId()).append("\",")
                .append("\"description\":\"").append(escape(order.getDescription())).append("\",")
                .append("\"currentAgentId\":\"")
                .append(order.getAssignedAgent() == null ? "" : order.getAssignedAgent().getId())
                .append("\"}\n");
        prompt.append("Available agents:\n");
        for (Agent agent : candidateAgents) {
            prompt.append("- {")
                    .append("\"id\":\"").append(agent.getId()).append("\",")
                    .append("\"name\":\"").append(escape(agent.getName())).append("\",")
                    .append("\"activeOrderCount\":").append(Optional.ofNullable(agent.getActiveOrderCount()).orElse(0))
                    .append("}\n");
        }
        prompt.append("Return only valid JSON with keys agentId, confidence, reasoning.");
        prompt.append(" Select only from the provided agent ids.");
        return prompt.toString();
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }
}
