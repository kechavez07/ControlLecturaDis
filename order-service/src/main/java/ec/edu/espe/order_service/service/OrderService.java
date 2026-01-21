package ec.edu.espe.order_service.service;

import ec.edu.espe.order_service.dto.OrderRequestDto;
import ec.edu.espe.order_service.dto.OrderResponseDto;

import java.util.UUID;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto request);

    OrderResponseDto getOrderById(UUID orderId);

    void updateOrderStatus(UUID orderId, String status, String reason);
}
