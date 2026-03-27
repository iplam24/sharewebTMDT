// src/main/java/com/shop/entity/Order.java
package com.shop.entity;

import com.shop.enums.OrderStatus;
import com.shop.enums.PaymentMethod;
import com.shop.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Index;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders",
        indexes = {
                // Tạo index trên user_id và status để tăng tốc độ tìm kiếm
                @Index(name = "idx_user_status", columnList = "user_id, status")
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ nhiều-1 với User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Quan hệ một-nhiều với OrderItem
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreationTimestamp // Tự động gán ngày giờ tạo
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING) // Lưu tên của Enum
    @Column(nullable = false)
    private OrderStatus status;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;

    private LocalDateTime deliveryDate;
    
    // Thông tin giao hàng
    @Column(nullable = false)
    private String shippingAddress;

    @Column(nullable = false)
    private String phoneNumber; // <-- THÊM TRƯỜNG NÀY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private Discount discount;

    // Helper method
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
