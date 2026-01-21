package ec.edu.espe.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAddressDto {

    private String country;
    private String city;
    private String street;
    private String postalCode;
}
