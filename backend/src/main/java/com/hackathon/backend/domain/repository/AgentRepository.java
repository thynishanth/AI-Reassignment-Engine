package com.hackathon.backend.domain.repository;

import com.hackathon.backend.domain.entity.Agent;
import com.hackathon.backend.domain.enums.AgentStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, String> {

    List<Agent> findByStatus(AgentStatus status);
}
