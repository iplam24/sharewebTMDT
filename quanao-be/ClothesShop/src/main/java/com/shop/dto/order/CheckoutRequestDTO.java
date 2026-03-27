package com.shop.dto.order;

// import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequestDTO {

    @NotNull(message = "Bạn phải chọn một địa chỉ giao hàng")
    private Long addressId;
    private String paymentMethod;
    private String discountCode; // Optional discount code
}
