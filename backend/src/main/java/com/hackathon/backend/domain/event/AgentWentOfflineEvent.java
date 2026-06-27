package com.hackathon.backend.domain.event;

import com.hackathon.backend.domain.entity.Agent;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AgentWentOfflineEvent extends ApplicationEvent {

    private final Agent agent;

    public AgentWentOfflineEvent(Object source, Agent agent) {
        super(source);
        this.agent = agent;
    }
}
