package ec.edu.espe.order_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponseDto {

    private UUID orderId;
    private UUID customerId;
    private String status;
    private String message;
    private String reason;
    private List<OrderItemResponseDto> items;
    private LocalDateTime updatedAt;
}
