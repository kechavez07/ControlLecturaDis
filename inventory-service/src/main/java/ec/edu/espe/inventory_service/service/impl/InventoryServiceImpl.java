package ec.edu.espe.inventory_service.service.impl;

import ec.edu.espe.inventory_service.dto.ProductStockResponseDto;
import ec.edu.espe.inventory_service.model.Product;
import ec.edu.espe.inventory_service.repository.ProductRepository;
import ec.edu.espe.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    @Override
    public ProductStockResponseDto getProductStock(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        return ProductStockResponseDto.builder()
                .productId(product.getId())
                .availableStock(product.getAvailableStock())
                .reservedStock(product.getReservedStock())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public boolean checkAndReserveStock(List<Map<String, Object>> items) {
        List<Product> productsToUpdate = new ArrayList<>();

        for (Map<String, Object> item : items) {
            UUID productId = UUID.fromString(item.get("productId").toString());
            int quantity = ((Number) item.get("quantity")).intValue();

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                log.warn("Product not found: {}", productId);
                return false;
            }

            Product product = productOpt.get();
            if (!product.hasAvailableStock(quantity)) {
                log.warn("Insufficient stock for product: {} (requested: {}, available: {})",
                        productId, quantity, product.getAvailableStock());
                return false;
            }

            product.reserveStock(quantity);
            productsToUpdate.add(product);
        }

        productRepository.saveAll(productsToUpdate);
        log.info("Stock reserved successfully for {} products", productsToUpdate.size());
        return true;
    }

    @Override
    public String getInsufficientStockReason(List<Map<String, Object>> items) {
        for (Map<String, Object> item : items) {
            UUID productId = UUID.fromString(item.get("productId").toString());
            int quantity = ((Number) item.get("quantity")).intValue();

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                return "Product not found: " + productId;
            }

            Product product = productOpt.get();
            if (!product.hasAvailableStock(quantity)) {
                return "Insufficient stock for product " + productId;
            }
        }
        return "Unknown reason";
    }
}
