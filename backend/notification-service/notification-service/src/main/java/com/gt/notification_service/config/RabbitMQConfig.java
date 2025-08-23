package com.gt.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "visitor_pass_exchange";
    public static final String QUEUE_PASSWORD_RESET_NAME = "password.reset.queue";
    public static final String ROUTING_KEY_PASSWORD_RESET = "password.reset";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // This bean tells RabbitMQ how to convert the JSON message back into a Java Object.
    // It's crucial for deserializing the PassApprovedEvent.
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "passwordResetQueue")
    public Queue passwordResetQueue() {
        return new Queue(QUEUE_PASSWORD_RESET_NAME, true);
    }

    @Bean
    public Binding passwordResetBinding(@Qualifier("passwordResetQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_PASSWORD_RESET);
    }
}