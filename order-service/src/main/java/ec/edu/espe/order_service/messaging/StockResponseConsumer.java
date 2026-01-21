package ec.edu.espe.order_service.messaging;

import ec.edu.espe.order_service.config.RabbitMQConfig;
import ec.edu.espe.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockResponseConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.STOCK_RESPONSE_QUEUE)
    public void handleStockResponse(StockResponseEvent event) {
        log.info("Received stock response event: {} for order: {}", event.getEventType(), event.getOrderId());

        try {
            if ("StockReserved".equals(event.getEventType())) {
                orderService.updateOrderStatus(event.getOrderId(), "CONFIRMED", null);
                log.info("Order {} confirmed successfully", event.getOrderId());
            } else if ("StockRejected".equals(event.getEventType())) {
                orderService.updateOrderStatus(event.getOrderId(), "CANCELLED", event.getReason());
                log.info("Order {} cancelled due to: {}", event.getOrderId(), event.getReason());
            }
        } catch (Exception e) {
            log.error("Error processing stock response for order {}: {}", event.getOrderId(), e.getMessage());
        }
    }
}
