package com.shop.service.order;

import com.shop.dto.order.CheckoutRequestDTO;
import com.shop.dto.order.OrderResponseDTO;
import com.shop.dto.response.PageResponseDTO;
import com.shop.enums.OrderStatus;
import com.shop.enums.PaymentStatus;

import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(CheckoutRequestDTO requestDTO);

    PageResponseDTO<OrderResponseDTO> getOrderHistoryForCurrentUser(int page, int size, String status);

    List<OrderResponseDTO> getAllOrders();

    OrderResponseDTO updateOrderStatus(Long orderId, OrderStatus status);

    OrderResponseDTO cancelOrder(Long orderId);

    void confirmPayment(Long orderId, PaymentStatus status);

    OrderResponseDTO confirmDelivery(Long orderId);

    // === NEW METHOD ===
    OrderResponseDTO getOrderById(Long orderId);
}
