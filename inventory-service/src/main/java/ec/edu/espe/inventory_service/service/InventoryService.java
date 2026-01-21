package ec.edu.espe.inventory_service.service;

import ec.edu.espe.inventory_service.dto.ProductStockResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface InventoryService {

    ProductStockResponseDto getProductStock(UUID productId);

    boolean checkAndReserveStock(List<Map<String, Object>> items);

    String getInsufficientStockReason(List<Map<String, Object>> items);
}
