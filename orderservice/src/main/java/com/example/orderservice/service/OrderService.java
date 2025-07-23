package com.example.orderservice.service;

import com.example.orderservice.event.OrderCreatedEvent;
import com.example.orderservice.event.PaymentResponseEvent;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderRequest;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    public String createOrder(OrderRequest req) {
        Order order = new Order();
        order.setStatus("PENDING");
        order.setAmount(req.getAmount());
        order = orderRepository.save(order);

        OrderCreatedEvent event = new OrderCreatedEvent(order.getId(), req.getAmount());
        kafkaTemplate.send("order-created", event);

        return "Order Created: " + order.getId();
    }

    @KafkaListener(topics = "payment-response", groupId = "order-service")
    public void consumePaymentResponse(PaymentResponseEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElseThrow();
        if (event.isPaymentSuccess()) {
            order.setStatus("COMPLETED");
        } else {
            order.setStatus("CANCELLED");
        }
        orderRepository.save(order);
        System.out.println("Order updated: " + order.getId() + " → " + order.getStatus());
    }
}
