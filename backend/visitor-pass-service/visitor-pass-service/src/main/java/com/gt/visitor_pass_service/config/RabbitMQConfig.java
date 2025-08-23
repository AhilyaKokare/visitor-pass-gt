package com.gt.visitor_pass_service.config;

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
    public static final String QUEUE_APPROVED_NAME = "pass.approved.queue";
    public static final String ROUTING_KEY_APPROVED = "pass.event.approved";
    public static final String QUEUE_REJECTED_NAME = "pass.rejected.queue";
    public static final String ROUTING_KEY_REJECTED = "pass.event.rejected";
    public static final String QUEUE_EXPIRED_NAME = "pass.expired.queue";
    public static final String ROUTING_KEY_EXPIRED = "pass.event.expired";
    public static final String QUEUE_USER_CREATED_NAME = "user.created.queue";
    public static final String ROUTING_KEY_USER_CREATED = "user.event.created";
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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean(name = "approvedQueue")
    public Queue approvedQueue() {
        return new Queue(QUEUE_APPROVED_NAME, true);
    }

    @Bean(name = "rejectedQueue")
    public Queue rejectedQueue() {
        return new Queue(QUEUE_REJECTED_NAME, true);
    }

    @Bean(name = "expiredQueue")
    public Queue expiredQueue() {
        return new Queue(QUEUE_EXPIRED_NAME, true);
    }

    @Bean(name = "userCreatedQueue")
    public Queue userCreatedQueue() {
        return new Queue(QUEUE_USER_CREATED_NAME, true);
    }

    @Bean(name = "passwordResetQueue")
    public Queue passwordResetQueue() {
        return new Queue(QUEUE_PASSWORD_RESET_NAME, true);
    }

    @Bean
    public Binding approvedBinding(@Qualifier("approvedQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_APPROVED);
    }

    @Bean
    public Binding rejectedBinding(@Qualifier("rejectedQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_REJECTED);
    }

    @Bean
    public Binding expiredBinding(@Qualifier("expiredQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_EXPIRED);
    }

    @Bean
    public Binding userCreatedBinding(@Qualifier("userCreatedQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_USER_CREATED);
    }

    @Bean
    public Binding passwordResetBinding(@Qualifier("passwordResetQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_PASSWORD_RESET);
    }
}