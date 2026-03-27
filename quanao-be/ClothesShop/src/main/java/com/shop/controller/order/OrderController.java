package com.shop.controller.order;

import com.shop.dto.order.CheckoutRequestDTO;
import com.shop.dto.order.OrderResponseDTO;
import com.shop.dto.response.PageResponseDTO; // Import DTO Phân trang
import com.shop.enums.OrderStatus;
import com.shop.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponseDTO> checkout(
            @Valid @RequestBody CheckoutRequestDTO requestDTO
    ) {
        OrderResponseDTO order = orderService.createOrder(requestDTO);
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @GetMapping("/my-history")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> getMyOrderHistory(
            // Frontend gửi lên: page (1, 2, 3...), size (5, 10...)
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String status
    ) {
        // Gọi Service với tham số
        PageResponseDTO<OrderResponseDTO> orders = orderService.getOrderHistoryForCurrentUser(
                page, size, status
        );
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<OrderResponseDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // === NEW API: Get Order Details ===
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long orderId) {
        OrderResponseDTO order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponseDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status
    ) {
        OrderResponseDTO order = orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.ok(order);
    }


    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponseDTO> cancelOrder(@PathVariable Long orderId) {
        OrderResponseDTO order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}/confirm-delivery")
    public ResponseEntity<OrderResponseDTO> confirmDelivery(@PathVariable Long orderId) {
        OrderResponseDTO order = orderService.confirmDelivery(orderId);
        return ResponseEntity.ok(order);
    }


}
