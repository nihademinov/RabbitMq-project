package org.example.rabbitmqproject.service;

import lombok.RequiredArgsConstructor;
import org.example.rabbitmqproject.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Receiver {

    private final DeadLetterService deadLetterService;

    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE_NAME)
    public void receiveMessage(String message) {
        try {
//            if(true)
//                throw new RuntimeException("This is a test");
            System.out.println("Received message: " + message);

        } catch (Exception e) {
            System.err.println("Error processing message: " + message);
            deadLetterService.listenDeadLetterQueue(message);
        }
    }
}
