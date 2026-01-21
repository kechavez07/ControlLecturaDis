package ec.edu.espe.inventory_service.config;

import ec.edu.espe.inventory_service.model.Product;
import ec.edu.espe.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            log.info("Loading sample products...");

            Product product1 = Product.builder()
                    .id(UUID.fromString("a3c2b1d0-6b0e-4f2b-9c1a-2d3f4a5b6c7d"))
                    .name("Laptop HP Pavilion")
                    .availableStock(25)
                    .reservedStock(0)
                    .build();

            Product product2 = Product.builder()
                    .id(UUID.fromString("b7e8c9d1-2f3a-4b5c-8d9e-1a2b3c4d5e6f"))
                    .name("Mouse Logitech MX")
                    .availableStock(50)
                    .reservedStock(0)
                    .build();

            Product product3 = Product.builder()
                    .id(UUID.fromString("c1d2e3f4-5a6b-7c8d-9e0f-1a2b3c4d5e6f"))
                    .name("Keyboard Mechanical RGB")
                    .availableStock(30)
                    .reservedStock(0)
                    .build();

            Product product4 = Product.builder()
                    .id(UUID.fromString("d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a"))
                    .name("Monitor Samsung 27")
                    .availableStock(15)
                    .reservedStock(0)
                    .build();

            Product product5 = Product.builder()
                    .id(UUID.fromString("e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b"))
                    .name("Webcam Logitech C920")
                    .availableStock(2)
                    .reservedStock(0)
                    .build();

            productRepository.save(product1);
            productRepository.save(product2);
            productRepository.save(product3);
            productRepository.save(product4);
            productRepository.save(product5);

            log.info("Sample products loaded successfully!");
        }
    }
}
