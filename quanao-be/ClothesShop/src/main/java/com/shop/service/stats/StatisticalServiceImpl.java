package com.shop.service.stats;

import com.shop.dto.stats.ProductSaleStatDTO;
import com.shop.dto.stats.RevenueByDateDTO;
import com.shop.dto.stats.TopProductDTO;
import com.shop.enums.OrderStatus;
import com.shop.repository.order.OrderRepository;
import com.shop.service.stats.StatisticalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticalServiceImpl implements StatisticalService {

    private final OrderRepository orderRepository;


    @Override
    public List<RevenueByDateDTO> getDailyRevenueLast30Days() {
        // Lấy thời điểm 30 ngày trước
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        // Gọi truy vấn đã được định nghĩa trong OrderRepository
        List<Object[]> results = orderRepository.getDailyRevenue(startDate);

        return results.stream()
                .map(row -> new RevenueByDateDTO(
                        ((Date) row[0]).toLocalDate(), // Chuyển SQL Date sang LocalDate
                        (BigDecimal) row[1]
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts(int limit) {
        // Gọi truy vấn đã được định nghĩa trong OrderRepository
        List<Object[]> results = orderRepository.findTopSellingProducts(PageRequest.of(0, limit));

        return results.stream()
                .map(row -> new TopProductDTO(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductSaleStatDTO> getSlowestSellingProducts(int limit) {
        List<Object[]> results = orderRepository.findSlowestSellingProducts(PageRequest.of(0, limit));

        return results.stream()
                .map(row -> new ProductSaleStatDTO(
                        (Long) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Long countDeliveredOrdersLast30Days() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        // Sử dụng truy vấn mới trong OrderRepository
        return orderRepository.countByStatusAndDeliveryDateGreaterThanEqual(
                OrderStatus.DELIVERED,
                startDate
        );
    }
}
