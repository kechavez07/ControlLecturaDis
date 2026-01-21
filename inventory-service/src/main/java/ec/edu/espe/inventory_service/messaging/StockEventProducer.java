package ec.edu.espe.inventory_service.messaging;

import ec.edu.espe.inventory_service.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendStockReserved(UUID orderId, UUID correlationId, List<OrderCreatedEvent.OrderItemEvent> items) {
        StockResponseEvent event = StockResponseEvent.builder()
                .eventType("StockReserved")
                .orderId(orderId)
                .correlationId(correlationId)
                .processedAt(LocalDateTime.now())
                .reservedItems(items.stream()
                        .map(item -> StockResponseEvent.ReservedItem.builder()
                                .productId(item.getProductId())
                                .quantity(item.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDERS_EXCHANGE,
                    RabbitMQConfig.STOCK_RESERVED_ROUTING_KEY,
                    event
            );
            log.info("StockReserved event sent for order: {}", orderId);
        } catch (Exception e) {
            log.error("Error sending StockReserved event: {}", e.getMessage());
        }
    }

    public void sendStockRejected(UUID orderId, UUID correlationId, String reason) {
        StockResponseEvent event = StockResponseEvent.builder()
                .eventType("StockRejected")
                .orderId(orderId)
                .correlationId(correlationId)
                .reason(reason)
                .processedAt(LocalDateTime.now())
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDERS_EXCHANGE,
                    RabbitMQConfig.STOCK_REJECTED_ROUTING_KEY,
                    event
            );
            log.info("StockRejected event sent for order: {} - Reason: {}", orderId, reason);
        } catch (Exception e) {
            log.error("Error sending StockRejected event: {}", e.getMessage());
        }
    }
}
