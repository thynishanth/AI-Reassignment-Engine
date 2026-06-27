package com.hackathon.backend.domain.repository;

import com.hackathon.backend.domain.entity.Order;
import com.hackathon.backend.domain.enums.OrderStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {

    @EntityGraph(attributePaths = "assignedAgent")
    List<Order> findByStatus(OrderStatus status);

    @EntityGraph(attributePaths = "assignedAgent")
    List<Order> findAll();

    List<Order> findByAssignedAgent_Id(String agentId);

    List<Order> findByAssignedAgent_IdAndStatus(String agentId, OrderStatus status);

    boolean existsByAssignedAgent_IdAndStatus(String agentId, OrderStatus status);
}
