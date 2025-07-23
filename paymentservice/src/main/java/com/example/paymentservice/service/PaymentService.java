package com.example.paymentservice.service;

import com.example.orderservice.event.OrderCreatedEvent;

import com.example.paymentservice.event.PaymentResponseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(topics = "order-created", groupId = "payment-service")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        System.out.println("Payment Service received OrderCreatedEvent: " + event.getOrderId());

        // Fake payment processing logic
        boolean paymentSuccess = true; // ya random logic

        PaymentResponseEvent response = new PaymentResponseEvent(event.getOrderId(), paymentSuccess);
        kafkaTemplate.send("payment-response", response);

        System.out.println("Payment Service sent PaymentResponseEvent for Order: " + event.getOrderId());
    }
}
