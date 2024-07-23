package org.example.rabbitmqproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;



import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@RequiredArgsConstructor
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "my-exchange";
    public static final String QUEUE_NAME = "my-queue";
    public static final String DLQ_NAME = "my-dlq";
    public static final String PARKING_LOT_QUEUE_NAME = "my-parking-lot";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPort(5672);
        return connectionFactory;
    }

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue queue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .deadLetterExchange(EXCHANGE_NAME)
                .deadLetterRoutingKey(DLQ_NAME)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ_NAME);
    }

    @Bean
    public Queue parkingLotQueue() {
        return new Queue(PARKING_LOT_QUEUE_NAME);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(QUEUE_NAME);
    }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, TopicExchange exchange) {
        return BindingBuilder.bind(deadLetterQueue).to(exchange).with(DLQ_NAME);
    }

    @Bean
    public Binding parkingLotBinding(Queue parkingLotQueue, TopicExchange exchange) {
        return BindingBuilder.bind(parkingLotQueue).to(exchange).with(PARKING_LOT_QUEUE_NAME);
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor(RabbitTemplate rabbitTemplate) {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5)
                .backOffOptions(2000, 2.0, 10000)
                .recoverer((message, cause) -> {
                    System.err.println("Message moved to DLQ: " + message);
                    rabbitTemplate.convertAndSend(EXCHANGE_NAME, DLQ_NAME, message);
                    return message;
                } )
                .build();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cachingConnectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            RetryOperationsInterceptor retryInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, cachingConnectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setAdviceChain(retryInterceptor);
        return factory;
    }
}