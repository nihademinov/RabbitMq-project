package org.example.rabbitmqproject.controller;

import org.example.rabbitmqproject.service.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private Sender sender;

    @PostMapping()
    public String sendMessage(@RequestParam String message) {
        sender.send(message);
        return "Message sent: " + message;
    }
}