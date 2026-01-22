package ec.edu.espe.order_service.service.impl;

import ec.edu.espe.order_service.dto.*;
import ec.edu.espe.order_service.messaging.OrderEventProducer;
import ec.edu.espe.order_service.model.Order;
import ec.edu.espe.order_service.model.OrderItem;
import ec.edu.espe.order_service.model.OrderStatus;
import ec.edu.espe.order_service.repository.OrderRepository;
import ec.edu.espe.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    @Override
    public List<OrderResponseDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::mapOrderToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .paymentReference(request.getPaymentReference())
                .build();

        if (request.getShippingAddress() != null) {
            order.setCountry(request.getShippingAddress().getCountry());
            order.setCity(request.getShippingAddress().getCity());
            order.setStreet(request.getShippingAddress().getStreet());
            order.setPostalCode(request.getShippingAddress().getPostalCode());
        }

        for (OrderItemRequestDto itemDto : request.getItems()) {
            OrderItem item = OrderItem.builder()
                    .productId(itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .build();
            order.addItem(item);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());

        orderEventProducer.sendOrderCreated(savedOrder);

        return OrderResponseDto.builder()
                .orderId(savedOrder.getId())
                .status(savedOrder.getStatus().name())
                .message("Order received. Inventory check in progress.")
                .build();
    }

    @Override
    public OrderResponseDto getOrderById(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        return mapOrderToResponseDto(order);
    }

    private OrderResponseDto mapOrderToResponseDto(Order order) {
        List<OrderItemResponseDto> items = order.getItems().stream()
                .map(item -> OrderItemResponseDto.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderResponseDto.OrderResponseDtoBuilder responseBuilder = OrderResponseDto.builder()
                .orderId(order.getId())
                .customerId(order.getCustomerId())
                .status(order.getStatus().name())
                .items(items)
                .updatedAt(order.getUpdatedAt());

        if (order.getStatus() == OrderStatus.PENDING) {
            responseBuilder.message("Inventory verification pending.");
        } else if (order.getStatus() == OrderStatus.CANCELLED && order.getReason() != null) {
            responseBuilder.reason(order.getReason());
        }

        return responseBuilder.build();
    }

    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, String status, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        order.setStatus(OrderStatus.valueOf(status));
        if (reason != null) {
            order.setReason(reason);
        }

        orderRepository.save(order);
        log.info("Order {} status updated to: {}", orderId, status);
    }
}
