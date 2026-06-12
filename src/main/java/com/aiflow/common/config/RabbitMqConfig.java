package com.aiflow.common.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "rag.document.exchange";
    public static final String QUEUE = "rag.document.parse.queue";
    public static final String ROUTING_KEY = "rag.document.parse";

    @Bean
    public DirectExchange ragExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue ragParseQueue() {
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public Binding ragBinding(DirectExchange ragExchange, Queue ragParseQueue) {
        return BindingBuilder.bind(ragParseQueue).to(ragExchange).with(ROUTING_KEY);
    }
}
