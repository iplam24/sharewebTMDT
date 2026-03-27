package com.shop.enums;

public enum OrderStatus {
    PENDING,        // Mới đặt, chờ xử lý
    PROCESSING,     // Đang xử lý (đóng gói)
    SHIPPED,        // Đang giao
    DELIVERED,      // Đã giao thành công
    CANCELLED       // Đã hủy
}
