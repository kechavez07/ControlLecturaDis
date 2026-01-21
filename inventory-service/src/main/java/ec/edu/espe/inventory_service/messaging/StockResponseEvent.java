package ec.edu.espe.inventory_service.messaging;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockResponseEvent {

    private String eventType;
    private UUID orderId;
    private UUID correlationId;
    private String reason;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime processedAt;

    private List<ReservedItem> reservedItems;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReservedItem {
        private UUID productId;
        private int quantity;
    }
}
