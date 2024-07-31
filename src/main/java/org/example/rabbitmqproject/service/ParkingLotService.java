package org.example.rabbitmqproject.service;

import org.example.rabbitmqproject.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ParkingLotService {
    @RabbitListener(queues = RabbitMQConfig.PARKING_LOT_QUEUE_NAME)
    public void listenParkingLotQueue(String message) {
        System.out.println("Received message in Parking Lot: " + message);
    }
}