package org.example.rabbitmqproject.service;

import org.example.rabbitmqproject.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterService {

    private final RabbitTemplate rabbitTemplate;

    public DeadLetterService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitMQConfig.DLQ_NAME)
    public void listenDeadLetterQueue(String message) {
        System.out.println("Received message in DLQ: " + message);

        try {
            if(true)
                throw new RuntimeException("This is a text parking lot");
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.QUEUE_NAME, message);
        } catch (Exception e) {
            System.err.println("Failed to process message from DLQ. Moving to Parking Lot: " + message);
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PARKING_LOT_QUEUE_NAME, message);
        }
    }
}