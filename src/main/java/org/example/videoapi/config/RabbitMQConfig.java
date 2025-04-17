package org.example.videoapi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "chat.exchange";
    public static final String QUEUE = "chat.queue";
    public static final String ROUTING_KEY = "chat.key";

    @Bean
    public DirectExchange chatExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue chatQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder.bind(chatQueue())
                .to(chatExchange())
                .with(ROUTING_KEY);
    }
}
