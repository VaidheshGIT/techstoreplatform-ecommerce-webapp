package com.example.notification.listener;

import com.example.notification.config.RabbitMQConfig;
import com.example.notification.event.OrderPlacedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleOrderPlacedEvent(OrderPlacedEvent orderPlacedEvent) {
        // Here we simulate sending an email or SMS to the user
        log.info("========================================");
        log.info("Received Notification Event!");
        log.info("Sending Order Confirmation Email for Order Number: {}", orderPlacedEvent.getOrderNumber());
        log.info("========================================");
    }
}
