package com.shop.repository.order;

import com.shop.entity.Discount;
import com.shop.entity.Order;
import com.shop.entity.User;
import com.shop.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // === CÁC PHƯƠNG THỨC CHO LOGIC ĐƠN HÀNG (KHÔI PHỤC LẠI) ===
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findAllByOrderByOrderDateDesc(); // <--- KHÔI PHỤC HÀM NÀY
    Page<Order> findByUserAndStatusOrderByOrderDateDesc(User user, OrderStatus status, Pageable pageable);
    Page<Order> findByUserOrderByOrderDateDesc(User user, Pageable pageable);
    boolean existsByUserAndStatusAndItemsProductId(User user, OrderStatus status, Long productId);
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.discount = :discount AND o.status != 'CANCELLED'")
    long countByUserAndDiscountAndStatusNotCancelled(@Param("user") User user, @Param("discount") Discount discount);

    // === CÁC PHƯƠG THỨC CHO THỐNG KÊ (GIỮ NGUYÊN) ===
    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED' AND o.deliveryDate >= :startDate")
    BigDecimal sumRevenueToday(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('DATE', o.deliveryDate) as date, SUM(o.totalPrice) as totalRevenue " +
            "FROM Order o " +
            "WHERE o.status = 'DELIVERED' AND o.deliveryDate >= :startDate " +
            "GROUP BY FUNCTION('DATE', o.deliveryDate) " +
            "ORDER BY date")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT FUNCTION('MONTH', o.deliveryDate) as month, SUM(o.totalPrice) as totalRevenue " +
            "FROM Order o " +
            "WHERE o.status = 'DELIVERED' AND FUNCTION('YEAR', o.deliveryDate) = :year " +
            "GROUP BY FUNCTION('MONTH', o.deliveryDate) " +
            "ORDER BY month")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    Long countByStatusAndDeliveryDateGreaterThanEqual(OrderStatus status, LocalDateTime deliveryDate);

    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi JOIN Order o ON oi.order.id = o.id " +
            "WHERE o.status != 'CANCELLED' " +
            "GROUP BY oi.productId, oi.productName " +
            "ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);

    @Query("SELECT oi.productId, oi.productName, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi JOIN Order o ON oi.order.id = o.id " +
            "WHERE o.status != 'CANCELLED' " +
            "GROUP BY oi.productId, oi.productName " +
            "ORDER BY totalSold ASC")
    List<Object[]> findSlowestSellingProducts(Pageable pageable);
}
