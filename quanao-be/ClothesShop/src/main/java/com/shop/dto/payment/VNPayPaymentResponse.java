package com.shop.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VNPayPaymentResponse {
    private String paymentUrl;
    private String message;
    // Bạn có thể thêm status code hoặc các thông tin khác nếu cần
}