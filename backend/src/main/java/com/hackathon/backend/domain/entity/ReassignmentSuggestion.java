package com.hackathon.backend.domain.entity;

import com.hackathon.backend.domain.enums.ReassignmentSuggestionStatus;
import com.hackathon.backend.domain.enums.TriggerReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jdk.jfr.DataAmount;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reassignment_suggestions")
@Data
public class ReassignmentSuggestion {

    @Id
    @Column(nullable = false, updatable = false, length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recommended_agent_id", nullable = false)
    private Agent recommendedAgent;

    @Column(name = "confidence_score", nullable = false, precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Column(nullable = false, length = 4000)
    private String reasoning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReassignmentSuggestionStatus status = ReassignmentSuggestionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_reason", nullable = false, length = 30)
    private TriggerReason triggerReason = TriggerReason.INITIAL;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
