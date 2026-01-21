package ec.edu.espe.inventory_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products_stock")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    @Column(name = "reserved_stock", nullable = false)
    @Builder.Default
    private int reservedStock = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean hasAvailableStock(int quantity) {
        return availableStock >= quantity;
    }

    public void reserveStock(int quantity) {
        if (!hasAvailableStock(quantity)) {
            throw new IllegalStateException("Insufficient stock");
        }
        availableStock -= quantity;
        reservedStock += quantity;
    }
}
