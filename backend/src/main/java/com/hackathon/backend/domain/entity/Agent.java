package com.hackathon.backend.domain.entity;

import com.hackathon.backend.domain.enums.AgentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "agents")
@Data
public class Agent {

    @Id
    @Column(nullable = false, updatable = false, length = 50)
    private String id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "active_order_count", nullable = false)
    private Integer activeOrderCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AgentStatus status = AgentStatus.AVAILABLE;

    @Column(name = "current_zone", length = 100)
    private String currentZone;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    public Agent() {
    }

}
