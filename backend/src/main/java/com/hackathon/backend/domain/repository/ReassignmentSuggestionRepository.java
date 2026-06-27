package com.hackathon.backend.domain.repository;

import com.hackathon.backend.domain.entity.ReassignmentSuggestion;
import com.hackathon.backend.domain.enums.ReassignmentSuggestionStatus;
import com.hackathon.backend.domain.enums.TriggerReason;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReassignmentSuggestionRepository extends JpaRepository<ReassignmentSuggestion, String> {

    Optional<ReassignmentSuggestion> findFirstByOrder_IdAndStatusAndTriggerReason(
            String orderId,
            ReassignmentSuggestionStatus status,
            TriggerReason triggerReason
    );

    @EntityGraph(attributePaths = {"order", "recommendedAgent"})
    List<ReassignmentSuggestion> findByStatusOrderByCreatedAtDesc(ReassignmentSuggestionStatus status);
}
