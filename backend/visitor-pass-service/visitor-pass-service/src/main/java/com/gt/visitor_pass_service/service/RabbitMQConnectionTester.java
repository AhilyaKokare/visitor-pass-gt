package com.gt.visitor_pass_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConnectionTester implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConnectionTester.class);

    private final RabbitAdmin rabbitAdmin;
    private final Queue expiredQueue;

    // We inject the RabbitAdmin and the specific Queue bean we are testing
    public RabbitMQConnectionTester(RabbitAdmin rabbitAdmin,
                                @Qualifier("rabbitExpiredQueue") Queue expiredQueue) {
        this.rabbitAdmin = rabbitAdmin;
        this.expiredQueue = expiredQueue;
    }

    @Override
    public void run(String... args) {
        logger.info("============================================================");
        logger.info("DIAGNOSTIC V2: Forcing direct declaration of queues...");

        if (rabbitAdmin == null) {
            logger.error("FATAL: RabbitAdmin bean is NULL. Auto-configuration is failing.");
            return;
        }
        if (expiredQueue == null) {
            logger.error("FATAL: expiredQueue bean is NULL. RabbitMQConfig is not being processed correctly.");
            return;
        }

        try {
            logger.info("Attempting to directly declare queue: '{}'", expiredQueue.getName());

            // This is a direct command: "CREATE THIS QUEUE"
            // It bypasses the automatic declaration process.
            rabbitAdmin.declareQueue(expiredQueue);

            logger.info("SUCCESS: Queue '{}' was declared successfully.", expiredQueue.getName());
            logger.info("Please REFRESH your RabbitMQ UI now. The queue should be visible.");
            logger.info("============================================================");
        } catch (Exception e) {
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            logger.error("DIAGNOSTIC V2: Direct declaration of queue '{}' FAILED.", expiredQueue.getName());
            logger.error("This is the definitive root cause of the problem.");
            logger.error("Underlying Exception Type: {}", e.getClass().getName());
            logger.error("Underlying Exception Message: {}", e.getMessage());
            logger.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            // We don't re-throw, we just log, so we can see the full application context.
        }
    }
}