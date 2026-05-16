package com.example.order.service;

import com.example.order.config.RabbitMQConfig;
import com.example.order.dto.OrderRequest;
import com.example.order.event.OrderPlacedEvent;
import com.example.order.model.Order;
import com.example.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final RabbitTemplate rabbitTemplate;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Transactional
    public String placeOrder(OrderRequest orderRequest) {
        // Call Product Service to reduce stock synchronously
        try {
            webClientBuilder.build().put()
                    .uri(productServiceUrl + "/api/products/" + orderRequest.getProductId() + "/reduce-stock?quantity=" + orderRequest.getQuantity())
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to reduce stock in Product Service or Product not found.", e);
        }

        // Save order
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setProductId(orderRequest.getProductId());
        order.setQuantity(orderRequest.getQuantity());
        order.setStatus("PLACED");

        orderRepository.save(order);

        // Send message to RabbitMQ asynchronously
        rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_QUEUE, new OrderPlacedEvent(order.getOrderNumber()));

        return "Order Placed Successfully. Order Number: " + order.getOrderNumber();
    }
}
