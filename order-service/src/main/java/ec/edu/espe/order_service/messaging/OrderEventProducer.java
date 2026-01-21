package ec.edu.espe.order_service.messaging;

import ec.edu.espe.order_service.config.RabbitMQConfig;
import ec.edu.espe.order_service.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendOrderCreated(Order order) {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .eventType("OrderCreated")
                .orderId(order.getId())
                .correlationId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .items(order.getItems().stream()
                        .map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDERS_EXCHANGE,
                    RabbitMQConfig.ORDER_CREATED_ROUTING_KEY,
                    event
            );
            log.info("OrderCreated event sent for order: {}", order.getId());
        } catch (Exception e) {
            log.error("Error sending OrderCreated event: {}", e.getMessage());
        }
    }
}
