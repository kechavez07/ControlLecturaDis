package ec.edu.espe.inventory_service.messaging;

import ec.edu.espe.inventory_service.config.RabbitMQConfig;
import ec.edu.espe.inventory_service.model.Product;
import ec.edu.espe.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final ProductRepository productRepository;
    private final StockEventProducer stockEventProducer;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreated event for order: {}", event.getOrderId());

        try {
            String insufficientStockReason = checkStockAvailability(event.getItems());

            if (insufficientStockReason != null) {
                stockEventProducer.sendStockRejected(
                        event.getOrderId(),
                        event.getCorrelationId(),
                        insufficientStockReason
                );
                return;
            }

            reserveStock(event.getItems());

            stockEventProducer.sendStockReserved(
                    event.getOrderId(),
                    event.getCorrelationId(),
                    event.getItems()
            );

        } catch (Exception e) {
            log.error("Error processing OrderCreated event for order {}: {}", event.getOrderId(), e.getMessage());
            stockEventProducer.sendStockRejected(
                    event.getOrderId(),
                    event.getCorrelationId(),
                    "Internal error processing order: " + e.getMessage()
            );
        }
    }

    private String checkStockAvailability(List<OrderCreatedEvent.OrderItemEvent> items) {
        for (OrderCreatedEvent.OrderItemEvent item : items) {
            Optional<Product> productOpt = productRepository.findById(item.getProductId());

            if (productOpt.isEmpty()) {
                return "Product not found: " + item.getProductId();
            }

            Product product = productOpt.get();
            if (!product.hasAvailableStock(item.getQuantity())) {
                return "Insufficient stock for product " + item.getProductId();
            }
        }
        return null;
    }

    private void reserveStock(List<OrderCreatedEvent.OrderItemEvent> items) {
        List<Product> productsToUpdate = new ArrayList<>();

        for (OrderCreatedEvent.OrderItemEvent item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductId()));

            product.reserveStock(item.getQuantity());
            productsToUpdate.add(product);
        }

        productRepository.saveAll(productsToUpdate);
        log.info("Stock reserved for {} products", productsToUpdate.size());
    }
}
