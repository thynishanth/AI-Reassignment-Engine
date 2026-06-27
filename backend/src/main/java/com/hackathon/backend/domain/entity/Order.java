package com.hackathon.backend.domain.entity;

import com.hackathon.backend.domain.enums.OrderStatus;
import com.hackathon.backend.domain.enums.WeightClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @Column(nullable = false, updatable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_agent_id", nullable = false)
    private Agent assignedAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.ASSIGNED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "pickup_zone", length = 100)
    private String pickupZone;

    @Column(name = "dropoff_zone", length = 100)
    private String dropoffZone;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_class", length = 20)
    private WeightClass weightClass;
}
