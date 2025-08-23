package com.gt.notification_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQErrorHandler implements RabbitListenerErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQErrorHandler.class);

    @Override
    public Object handleError(org.springframework.amqp.core.Message amqpMessage,
                              org.springframework.messaging.Message<?> message,
                              ListenerExecutionFailedException exception) throws Exception {

        logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        logger.error("RABBITMQ LISTENER FAILED");
        logger.error("This is likely due to a JSON deserialization error (DTO mismatch).");
        logger.error("Cause: {}", exception.getCause().getMessage());
        logger.error("Failed Message Payload: {}", new String(amqpMessage.getBody()));
        logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        // Re-throwing the exception will cause the message to be rejected.
        // Depending on your broker config, it might go to a Dead Letter Queue.
        throw exception;
    }
}