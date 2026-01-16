package com.example.librarian.controller;

import com.example.librarian.service.KafkaProducerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    private final KafkaProducerService producerService;

    public TestController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @GetMapping("/api/test")
    public String healthCheck() {
        return "Librarian App is running! Kafka is ready.";
    }

    @GetMapping("/api/kafka/test")
    public String testKafka(@RequestParam(value = "message", defaultValue = "Test Message") String message) {
        producerService.sendBookEvent(message);
        return "Message sent to Kafka: " + message;
    }
}