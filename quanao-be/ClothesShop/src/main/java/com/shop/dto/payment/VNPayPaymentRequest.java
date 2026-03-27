package com.shop.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VNPayPaymentRequest {
    @NotNull(message = "Order ID không được để trống")
    private Long orderId;

    @Min(value = 1000, message = "Số tiền phải lớn hơn 1000 VND") // VNPay thường yêu cầu tối thiểu
    private long amount;

    @NotBlank(message = "Thông tin đơn hàng không được để trống")
    private String orderInfo;

    // Bạn có thể thêm các trường khác nếu cần, ví dụ:
    // private String bankCode;
    // private String language; // "vn" hoặc "en"
}