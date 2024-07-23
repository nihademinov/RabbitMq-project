package org.example.rabbitmqproject.controller;

import org.example.rabbitmqproject.service.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    @Autowired
    private Sender sender;

    @GetMapping("/send")
    public String sendMessage(@RequestParam String message) {
        sender.send(message);
        return "Message sent: " + message;
    }
}