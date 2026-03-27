package com.shop.service.stats;

import com.shop.dto.stats.ProductSaleStatDTO;
import com.shop.dto.stats.RevenueByDateDTO;
import com.shop.dto.stats.TopProductDTO;
import com.shop.enums.OrderStatus; // Cần import

import java.math.BigDecimal;
import java.util.List;

public interface StatisticalService {
    List<RevenueByDateDTO> getDailyRevenueLast30Days();
    List<TopProductDTO> getTopSellingProducts(int limit);
    List<ProductSaleStatDTO> getSlowestSellingProducts(int limit);

    // THÊM HÀM MỚI: Đếm tổng số đơn hàng đã giao
    Long countDeliveredOrdersLast30Days();
}
