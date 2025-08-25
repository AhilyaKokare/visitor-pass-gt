package com.gt.visitor_pass_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // This is the topic prefix clients will subscribe to
        config.enableSimpleBroker("/topic");
        // This is the prefix for messages from clients to the server (we won't use this for now)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // This is the HTTP endpoint that the client will connect to to upgrade to WebSockets
        registry.addEndpoint("/ws")
                .setAllowedOrigins("http://localhost:4200") // Allow our Angular app to connect
                .withSockJS(); // Use SockJS for fallback compatibility
    }
}