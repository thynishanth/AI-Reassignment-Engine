package com.hackathon.backend.service;

import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.entity.Order;
import com.hackathon.backend.domain.enums.AgentStatus;
import com.hackathon.backend.domain.enums.OrderStatus;
import com.hackathon.backend.domain.enums.TriggerReason;
import com.hackathon.backend.domain.repository.AgentRepository;
import com.hackathon.backend.domain.repository.OrderRepository;
import com.hackathon.backend.web.dto.request.CreateOrderRequest;
import com.hackathon.backend.web.dto.response.OrderResponse;
import com.hackathon.backend.web.dto.response.ReassignmentSuggestionResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AgentRepository agentRepository;
    private final ReassignmentSuggestionService suggestionService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Agent agent = agentRepository.findById(request.assignedAgentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assigned agent not found"));

        Order order = new Order();
        order.setId(request.id());
        order.setDescription(request.description());
        order.setAssignedAgent(agent);
        order.setStatus(OrderStatus.ASSIGNED);

        agent.setActiveOrderCount(safeCount(agent.getActiveOrderCount()) + 1);
        agentRepository.save(agent);

        return toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(OrderStatus status) {
        return (status == null ? orderRepository.findAll() : orderRepository.findByStatus(status))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ReassignmentSuggestionResponse suggest(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        return suggestionService.createAndPersistSuggestion(
                order,
                agentRepository.findByStatus(AgentStatus.AVAILABLE),
                TriggerReason.INITIAL
        );
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getDescription(),
                order.getAssignedAgent() == null ? null : order.getAssignedAgent().getId(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    private int safeCount(Integer count) {
        return Optional.ofNullable(count).orElse(0);
    }
}
