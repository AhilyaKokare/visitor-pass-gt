package com.gt.visitor_pass_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WebSocketUpdateService {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketUpdateService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyDashboardUpdate(Long tenantId) {
        String topic = "/topic/dashboard/" + tenantId;
        // We send a simple message. The content doesn't matter, just the fact that a message was sent.
        Map<String, String> message = Map.of("message", "A pass has been updated. Please refresh.");
        messagingTemplate.convertAndSend(topic, message);
    }
}