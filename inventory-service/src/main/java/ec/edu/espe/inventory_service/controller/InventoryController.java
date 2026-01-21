package ec.edu.espe.inventory_service.controller;

import ec.edu.espe.inventory_service.dto.ProductStockResponseDto;
import ec.edu.espe.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}/stock")
    public ResponseEntity<ProductStockResponseDto> getProductStock(@PathVariable UUID productId) {
        ProductStockResponseDto response = inventoryService.getProductStock(productId);
        return ResponseEntity.ok(response);
    }
}
