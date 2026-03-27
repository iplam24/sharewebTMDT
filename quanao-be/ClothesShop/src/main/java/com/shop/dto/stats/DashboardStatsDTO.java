package com.shop.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalOrders;           // Tổng số đơn hàng
    private long deliveredOrders;       // Số đơn đã giao thành công
    private long totalStock;            // Tổng sản phẩm tồn kho
    private long newUsersThisWeek;      // User mới trong 7 ngày qua
    private BigDecimal totalRevenue;    // Tổng doanh thu toàn thời gian
    private BigDecimal revenueToday;    // Doanh thu hôm nay
}
