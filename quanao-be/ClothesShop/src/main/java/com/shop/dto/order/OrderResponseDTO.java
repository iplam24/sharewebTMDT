package com.shop.dto.order;

import com.shop.enums.OrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.shop.enums.PaymentMethod;
import com.shop.enums.PaymentStatus;

@Data
public class OrderResponseDTO {
    private Long id;
    private String username;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String phoneNumber; // <-- THÊM TRƯỜNG NÀY
    private List<OrderItemResponseDTO> items;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String discountCode;
    private BigDecimal discountAmount;
}
