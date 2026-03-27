package com.shop.controller.stats;

import com.shop.dto.stats.DashboardStatsDTO;
import com.shop.dto.stats.ProductSaleStatDTO;
import com.shop.dto.stats.RevenueStatsDTO;
import com.shop.enums.OrderStatus;
import com.shop.repository.order.OrderRepository;
import com.shop.repository.product.ProductVariantRepository;
import com.shop.repository.user.UserRepository;
import com.shop.service.stats.StatisticalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticalController {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final StatisticalService statisticalService;

    // API 1: Thống kê tổng quan cho Dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        // 1. Tổng đơn hàng
        long totalOrders = orderRepository.count();

        // 2. Đơn hàng đã giao
        long deliveredOrders = orderRepository.countByStatus(OrderStatus.DELIVERED);

        // 3. Tổng tồn kho
        Long totalStock = productVariantRepository.sumTotalStock();
        if (totalStock == null) totalStock = 0L;

        // 4. User mới trong 7 ngày
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        long newUsers = userRepository.countByCreatedAtAfter(sevenDaysAgo);

        // 5. Tổng doanh thu
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // 6. Doanh thu hôm nay
        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        BigDecimal revenueToday = orderRepository.sumRevenueToday(startOfToday);
        if (revenueToday == null) revenueToday = BigDecimal.ZERO;

        return ResponseEntity.ok(DashboardStatsDTO.builder()
                .totalOrders(totalOrders)
                .deliveredOrders(deliveredOrders)
                .totalStock(totalStock)
                .newUsersThisWeek(newUsers)
                .totalRevenue(totalRevenue)
                .revenueToday(revenueToday)
                .build());
    }

    // API 2: Biểu đồ doanh thu theo ngày (30 ngày gần nhất)
    @GetMapping("/revenue")
    public ResponseEntity<List<RevenueStatsDTO>> getRevenueStats(
            @RequestParam(defaultValue = "30") int days
    ) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<Object[]> results = orderRepository.getDailyRevenue(startDate);

        List<RevenueStatsDTO> stats = results.stream()
                .map(row -> new RevenueStatsDTO(
                        row[0].toString(), // Date
                        (BigDecimal) row[1] // Revenue
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(stats);
    }

    // API 3: Biểu đồ doanh thu theo THÁNG (trong năm được chọn)
    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<RevenueStatsDTO>> getMonthlyRevenueStats(
            @RequestParam(defaultValue = "0") int year
    ) {
        // Nếu không truyền năm, lấy năm hiện tại
        if (year == 0) {
            year = LocalDate.now().getYear();
        }

        List<Object[]> results = orderRepository.getMonthlyRevenue(year);

        // Tạo danh sách 12 tháng mặc định là 0
        List<RevenueStatsDTO> stats = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            stats.add(new RevenueStatsDTO("Tháng " + i, BigDecimal.ZERO));
        }

        // Điền dữ liệu thực tế vào
        for (Object[] row : results) {
            int month = (int) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            // month chạy từ 1-12, index chạy từ 0-11
            stats.get(month - 1).setRevenue(revenue);
        }

        return ResponseEntity.ok(stats);
    }

    // API 4: Top sản phẩm bán chạy
    @GetMapping("/top-products")
    public ResponseEntity<List<Object[]>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(orderRepository.findTopSellingProducts(PageRequest.of(0, limit)));
    }

    // API 5: Top sản phẩm bán chậm
    @GetMapping("/slowest-selling-products")
    public ResponseEntity<List<ProductSaleStatDTO>> getSlowestSellingProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(statisticalService.getSlowestSellingProducts(limit));
    }
}
